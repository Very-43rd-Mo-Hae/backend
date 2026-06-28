package com.very.relink.notification.application.command;

public record DisableWebPushSubscriptionCommand(
        Long userId,
        String endpoint
) {
}
