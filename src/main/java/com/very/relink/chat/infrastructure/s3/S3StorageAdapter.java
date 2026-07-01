package com.very.relink.chat.infrastructure.s3;

import com.very.relink.chat.application.command.ChatCommands.IssueChatAttachmentPresignedUrlCommand;
import com.very.relink.chat.application.command.ChatCommands.IssueProfileImagePresignedUrlCommand;
import com.very.relink.chat.application.port.ChatPorts.StoragePresignedUrlPort;
import com.very.relink.chat.application.port.ChatPorts.StorageUrlResolver;
import com.very.relink.chat.application.response.ChatResponses.PresignedUploadUrl;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@ConditionalOnProperty(prefix = "chat.storage", name = "type", havingValue = "s3")
public class S3StorageAdapter implements StoragePresignedUrlPort, StorageUrlResolver {

    private final S3ChatStorageProperties properties;
    private final S3Presigner presigner;
    private final String publicBaseUrl;

    public S3StorageAdapter(S3ChatStorageProperties properties) {
        validateProperties(properties);
        this.properties = properties;
        this.presigner = S3Presigner.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.publicBaseUrl = trimTrailingSlash(properties.publicBaseUrl());
    }

    @Override
    public PresignedUploadUrl issueUploadUrl(IssueChatAttachmentPresignedUrlCommand command) {
        String storageKey = createStorageKey(command.roomId(), command.fileName(), command.contentType());
        return issuePresignedPutUrl(storageKey, command.contentType());
    }

    @Override
    public PresignedUploadUrl issueProfileImageUploadUrl(IssueProfileImagePresignedUrlCommand command) {
        String storageKey = createProfileStorageKey(command.requesterId(), command.fileName(), command.contentType());
        return issuePresignedPutUrl(storageKey, command.contentType());
    }

    @Override
    public String resolveUrl(String storageKey) {
        return publicBaseUrl + "/" + storageKey;
    }

    private PresignedUploadUrl issuePresignedPutUrl(String storageKey, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(storageKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.uploadExpiresIn()))
                .putObjectRequest(objectRequest)
                .build();

        String uploadUrl = presigner.presignPutObject(presignRequest).url().toString();
        return new PresignedUploadUrl(uploadUrl, storageKey, properties.uploadExpiresIn());
    }

    @PreDestroy
    void closePresigner() {
        presigner.close();
    }

    private void validateProperties(S3ChatStorageProperties properties) {
        if (properties.bucket() == null || properties.bucket().isBlank()) {
            throw new IllegalStateException("chat.storage.s3.bucket must be configured when S3 storage is enabled.");
        }
        if (properties.publicBaseUrl() == null || properties.publicBaseUrl().isBlank()) {
            throw new IllegalStateException("chat.storage.s3.public-base-url must be configured when S3 storage is enabled.");
        }
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
