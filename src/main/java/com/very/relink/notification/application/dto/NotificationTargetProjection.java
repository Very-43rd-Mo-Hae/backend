package com.very.relink.notification.application.dto;

import com.very.relink.notification.domain.model.PushProvider;

public record NotificationTargetProjection(
        Long id,
        Long userId,
        String endpoint,
        String p256dh,
        String auth,
        PushProvider provider
) {
}
