package com.very.relink.notification.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationOutbox {

    private final Long id;
    private Long notificationId;
    private Long userId;
    private NotificationChannel channel;
    private String title;
    private String body;
    private String linkUrl;
    private Long deduplicationId;
    private String dataJson;
    private NotificationOutboxStatus status;
    private int attemptCount;
    private String failureReason;

    public static NotificationOutbox pendingWebPush(
            Long notificationId,
            Long userId,
            String title,
            String body,
            String linkUrl,
            Long deduplicationId,
            String dataJson
    ) {
        return NotificationOutbox.builder()
                .notificationId(notificationId)
                .userId(userId)
                .channel(NotificationChannel.WEB_PUSH)
                .title(title)
                .body(body)
                .linkUrl(linkUrl)
                .deduplicationId(deduplicationId)
                .dataJson(dataJson)
                .status(NotificationOutboxStatus.PENDING)
                .build();
    }
}
