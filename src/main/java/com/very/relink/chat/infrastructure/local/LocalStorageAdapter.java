package com.very.relink.chat.infrastructure.local;

import com.very.relink.chat.application.command.ChatCommands.IssueChatAttachmentPresignedUrlCommand;
import com.very.relink.chat.application.command.ChatCommands.IssueProfileImagePresignedUrlCommand;
import com.very.relink.chat.application.port.ChatPorts.StoragePresignedUrlPort;
import com.very.relink.chat.application.port.ChatPorts.StorageUrlResolver;
import com.very.relink.chat.application.response.ChatResponses.PresignedUploadUrl;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "chat.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalStorageAdapter implements StoragePresignedUrlPort, StorageUrlResolver {

    private final LocalChatStorageProperties properties;

    public LocalStorageAdapter(LocalChatStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public PresignedUploadUrl issueUploadUrl(IssueChatAttachmentPresignedUrlCommand command) {
        String storageKey = createStorageKey(command.roomId(), command.fileName(), command.contentType());
        String encodedKey = URLEncoder.encode(storageKey, StandardCharsets.UTF_8);

        return new PresignedUploadUrl(
                "/api/v1/chat/attachments/local?key=" + encodedKey,
                storageKey,
                properties.uploadExpiresIn()
        );
    }

    @Override
    public PresignedUploadUrl issueProfileImageUploadUrl(IssueProfileImagePresignedUrlCommand command) {
        String storageKey = createProfileStorageKey(command.requesterId(), command.fileName(), command.contentType());
        String encodedKey = URLEncoder.encode(storageKey, StandardCharsets.UTF_8);

        return new PresignedUploadUrl(
                "/api/v1/chat/attachments/local?key=" + encodedKey,
                storageKey,
                properties.uploadExpiresIn()
        );
    }

    @Override
    public String resolveUrl(String storageKey) {
        return trimTrailingSlash(properties.publicBaseUrl()) + "/" + storageKey;
    }

    private String createStorageKey(Long roomId, String fileName, String contentType) {
        LocalDate now = LocalDate.now();
        return "chat/rooms/%d/%d/%02d/%s.%s".formatted(
                roomId,
                now.getYear(),
                now.getMonthValue(),
                UUID.randomUUID(),
                extension(fileName, contentType)
        );
    }

    private String createProfileStorageKey(Long memberId, String fileName, String contentType) {
        LocalDate now = LocalDate.now();
        return "members/%d/profile/%d/%02d/%s.%s".formatted(
                memberId,
                now.getYear(),
                now.getMonthValue(),
                UUID.randomUUID(),
                extension(fileName, contentType)
        );
    }

    private String extension(String fileName, String contentType) {
        if (fileName != null && fileName.contains(".")) {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("webp")) {
                return ext.equals("jpg") ? "jpeg" : ext;
            }
        }
        return switch (contentType) {
            case "image/jpeg" -> "jpeg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "bin";
        };
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
