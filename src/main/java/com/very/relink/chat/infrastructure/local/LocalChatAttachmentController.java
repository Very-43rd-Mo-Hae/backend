package com.very.relink.chat.infrastructure.local;

import com.very.relink.chat.exception.ChatErrorCode;
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
public class LocalChatAttachmentController {

    private static final long MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024L;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path rootPath;

    public LocalChatAttachmentController(LocalChatStorageProperties properties) {
        this.rootPath = Path.of(properties.rootPath()).toAbsolutePath().normalize();
    }

    @PutMapping
    public ResponseEntity<Void> upload(
            @RequestParam String key,
            @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
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
