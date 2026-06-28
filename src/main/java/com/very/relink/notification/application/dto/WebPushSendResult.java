package com.very.relink.notification.application.dto;

import lombok.Builder;

@Builder
public record WebPushSendResult(
        int total,
        int success,
        int failure,
        int expired,
        boolean skippedByDeduplication
) {
    public static WebPushSendResult skipped() {
        return WebPushSendResult.builder()
                .skippedByDeduplication(true)
                .build();
    }
}
