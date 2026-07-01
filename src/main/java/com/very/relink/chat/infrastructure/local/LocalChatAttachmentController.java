package com.very.relink.chat.infrastructure.local;

import com.very.relink.chat.exception.ChatErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/chat/attachments/local")
@ConditionalOnProperty(prefix = "chat.storage", name = "type", havingValue = "local", matchIfMissing = true)
@Tag(name = "Chat Local Attachment", description = "로컬 개발 환경용 채팅 첨부 파일 업로드 API")
public class LocalChatAttachmentController {

    private static final long MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024L;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path rootPath;

    public LocalChatAttachmentController(LocalChatStorageProperties properties) {
        this.rootPath = Path.of(properties.rootPath()).toAbsolutePath().normalize();
    }

    @PutMapping
    @Operation(
            summary = "로컬 채팅 첨부 파일 업로드",
            description = "로컬 스토리지 설정에서 presigned URL 대신 사용할 채팅 첨부 파일 업로드 엔드포인트입니다. JPEG, PNG, WebP 이미지만 업로드할 수 있습니다."
    )
    public ResponseEntity<Void> upload(
            @Parameter(description = "저장할 첨부 파일 storageKey", example = "chat/attachments/1/example.png")
            @RequestParam String key,
            @Parameter(description = "업로드 파일 Content-Type", example = "image/png")
            @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업로드할 이미지 파일 바이너리",
                    required = true
            )
            @RequestBody byte[] bytes
    ) {
        validateUpload(key, contentType, bytes);

        try {
            Path targetPath = resolveStoragePath(key);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, bytes);
            return ResponseEntity.noContent().build();
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store chat attachment.", ex);
        }
    }

    private void validateUpload(String key, String contentType, byte[] bytes) {
        if (key == null || key.isBlank() || key.contains("\\") || key.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage key.");
        }
        if (!IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw ChatErrorCode.UNSUPPORTED_ATTACHMENT_CONTENT_TYPE.toException();
        }
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_IMAGE_FILE_SIZE) {
            throw ChatErrorCode.ATTACHMENT_FILE_TOO_LARGE.toException();
        }
    }

    private Path resolveStoragePath(String storageKey) {
        Path targetPath = rootPath.resolve(storageKey).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage key.");
        }
        return targetPath;
    }
}
