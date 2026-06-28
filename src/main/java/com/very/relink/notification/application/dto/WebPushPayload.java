package com.very.relink.notification.application.dto;

import java.util.Map;

public record WebPushPayload(
        String title,
        String body,
        String linkUrl,
        Long notificationId,
        Map<String, Object> data
) {
}
