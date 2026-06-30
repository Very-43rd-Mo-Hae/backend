package com.very.relink.chat.infrastructure.local;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat.storage.local")
public record LocalChatStorageProperties(
        String rootPath,
        String publicBaseUrl,
        long uploadExpiresIn
) {
    public LocalChatStorageProperties {
        if (rootPath == null || rootPath.isBlank()) {
            rootPath = "build/local-storage";
        }
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            publicBaseUrl = "http://localhost:8080/uploads";
        }
        if (uploadExpiresIn <= 0) {
            uploadExpiresIn = 300;
        }
    }
}
