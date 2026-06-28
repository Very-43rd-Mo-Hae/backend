package com.very.relink.notification.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record TestWebPushNotificationRequest(
        @NotBlank String title,
        @NotBlank String body,
        String linkUrl,
        Long notificationId,
        Map<String, Object> data
) {
}
