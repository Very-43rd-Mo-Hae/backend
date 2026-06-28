package com.very.relink.chat.infrastructure.s3;

import com.very.relink.chat.application.command.ChatCommands.IssueChatAttachmentPresignedUrlCommand;
import com.very.relink.chat.application.port.ChatPorts.StoragePresignedUrlPort;
import com.very.relink.chat.application.port.ChatPorts.StorageUrlResolver;
import com.very.relink.chat.application.response.ChatResponses.PresignedUploadUrl;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3StorageAdapter implements StoragePresignedUrlPort, StorageUrlResolver {

    private final String publicBaseUrl;
    private final String uploadBaseUrl;
    private final long expiresIn;

    public S3StorageAdapter(
            @Value("${chat.storage.public-base-url:https://storage.example.com}") String publicBaseUrl,
            @Value("${chat.storage.upload-base-url:https://s3-presigned-put-url.example.com}") String uploadBaseUrl,
            @Value("${chat.storage.presigned-expires-in:300}") long expiresIn
    ) {
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.uploadBaseUrl = trimTrailingSlash(uploadBaseUrl);
        this.expiresIn = expiresIn;
    }

    @Override
    public PresignedUploadUrl issueUploadUrl(IssueChatAttachmentPresignedUrlCommand command) {
        String storageKey = createStorageKey(command.roomId(), command.fileName(), command.contentType());
        return new PresignedUploadUrl(uploadBaseUrl + "/" + storageKey, storageKey, expiresIn);
    }

    @Override
    public String resolveUrl(String storageKey) {
        return publicBaseUrl + "/" + storageKey;
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
