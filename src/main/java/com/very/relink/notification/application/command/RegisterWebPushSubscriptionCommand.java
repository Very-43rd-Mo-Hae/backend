package com.very.relink.notification.application.command;

public record RegisterWebPushSubscriptionCommand(
        Long userId,
        String endpoint,
        String p256dh,
        String auth,
        String userAgent
) {
}
