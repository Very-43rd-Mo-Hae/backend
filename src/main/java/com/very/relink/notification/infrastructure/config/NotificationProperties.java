package com.very.relink.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(
        WebPush webPush,
        Redis redis,
        Outbox outbox
) {
    public record WebPush(
            boolean enabled,
            String subject,
            String publicKey,
            String privateKey
    ) {
    }

    public record Redis(
            String dedupPrefix,
            long dedupTtlSeconds
    ) {
    }

    public record Outbox(
            boolean enabled,
            int batchSize,
            long fixedDelayMillis
    ) {
    }
}
