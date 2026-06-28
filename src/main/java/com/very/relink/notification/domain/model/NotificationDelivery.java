package com.very.relink.notification.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationDelivery {

    private final Long id;
    private Long notificationId;
    private Long outboxId;
    private Long targetId;
    private Long userId;
    private NotificationChannel channel;
    private PushProvider provider;
    private NotificationDeliveryStatus status;
    private String failureReason;
    private LocalDateTime sentAt;

    public static NotificationDelivery success(
            Long notificationId,
            Long outboxId,
            Long targetId,
            Long userId,
            PushProvider provider,
            LocalDateTime sentAt
    ) {
        return create(notificationId, outboxId, targetId, userId, provider, NotificationDeliveryStatus.SUCCESS, null, sentAt);
    }

    public static NotificationDelivery failed(
            Long notificationId,
            Long outboxId,
            Long targetId,
            Long userId,
            PushProvider provider,
            NotificationDeliveryStatus status,
            String failureReason,
            LocalDateTime sentAt
    ) {
        return create(notificationId, outboxId, targetId, userId, provider, status, failureReason, sentAt);
    }

    private static NotificationDelivery create(
            Long notificationId,
            Long outboxId,
            Long targetId,
            Long userId,
            PushProvider provider,
            NotificationDeliveryStatus status,
            String failureReason,
            LocalDateTime sentAt
    ) {
        return NotificationDelivery.builder()
                .notificationId(notificationId)
                .outboxId(outboxId)
                .targetId(targetId)
                .userId(userId)
                .channel(NotificationChannel.WEB_PUSH)
                .provider(provider)
                .status(status)
                .failureReason(failureReason)
                .sentAt(sentAt)
                .build();
    }
}
