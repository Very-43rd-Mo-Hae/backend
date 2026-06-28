package com.very.relink.notification.application.dto;

public record NotificationTargetProjection(
        Long id,
        Long userId,
        String endpoint,
        String p256dh,
        String auth
) {
}
