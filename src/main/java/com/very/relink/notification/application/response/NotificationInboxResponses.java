package com.very.relink.notification.application.response;

import java.time.LocalDateTime;
import java.util.List;

public final class NotificationInboxResponses {

    private NotificationInboxResponses() {
    }

    public record NotificationInboxResponse(
            List<NotificationInboxItemResponse> notifications,
            int page,
            int size,
            boolean hasNext,
            boolean hasUnread
    ) {
    }

    public record NotificationInboxItemResponse(
            Long id,
            Long notificationId,
            String title,
            String body,
            String linkUrl,
            boolean read,
            LocalDateTime createdAt,
            LocalDateTime readAt
    ) {
    }
}
