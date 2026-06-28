package com.very.relink.notification.application.command;

import java.util.Map;

public record SendWebPushNotificationCommand(
        Long userId,
        String title,
        String body,
        String linkUrl,
        Long notificationId,
        Map<String, Object> data
) {
}
