package com.very.relink.chat.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat")
public record ChatProperties(
        Redis redis,
        Outbox outbox
) {
    public record Redis(
            String streamKey
    ) {
    }

    public record Outbox(
            boolean enabled,
            long fixedDelayMillis
    ) {
    }
}
