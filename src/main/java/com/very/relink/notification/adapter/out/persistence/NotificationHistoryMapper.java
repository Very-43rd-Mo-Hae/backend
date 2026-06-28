package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.notification.domain.model.NotificationDelivery;
import com.very.relink.notification.domain.model.NotificationMessage;
import com.very.relink.notification.domain.model.NotificationOutbox;
import org.springframework.stereotype.Component;

@Component
public class NotificationHistoryMapper {

    public NotificationJpaEntity toEntity(NotificationMessage notification) {
        return NotificationJpaEntity.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .linkUrl(notification.getLinkUrl())
                .deduplicationId(notification.getDeduplicationId())
                .status(notification.getStatus())
                .build();
    }

    public NotificationMessage toDomain(NotificationJpaEntity entity) {
        return NotificationMessage.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .linkUrl(entity.getLinkUrl())
                .deduplicationId(entity.getDeduplicationId())
                .status(entity.getStatus())
                .build();
    }

    public NotificationOutboxJpaEntity toEntity(NotificationOutbox outbox) {
        return NotificationOutboxJpaEntity.builder()
                .id(outbox.getId())
                .notificationId(outbox.getNotificationId())
                .userId(outbox.getUserId())
                .channel(outbox.getChannel())
                .title(outbox.getTitle())
                .body(outbox.getBody())
                .linkUrl(outbox.getLinkUrl())
                .deduplicationId(outbox.getDeduplicationId())
                .dataJson(outbox.getDataJson())
                .status(outbox.getStatus())
                .attemptCount(outbox.getAttemptCount())
                .failureReason(outbox.getFailureReason())
                .build();
    }

    public NotificationOutbox toDomain(NotificationOutboxJpaEntity entity) {
        return NotificationOutbox.builder()
                .id(entity.getId())
                .notificationId(entity.getNotificationId())
                .userId(entity.getUserId())
                .channel(entity.getChannel())
                .title(entity.getTitle())
                .body(entity.getBody())
                .linkUrl(entity.getLinkUrl())
                .deduplicationId(entity.getDeduplicationId())
                .dataJson(entity.getDataJson())
                .status(entity.getStatus())
                .attemptCount(entity.getAttemptCount())
                .failureReason(entity.getFailureReason())
                .build();
    }

    public NotificationDeliveryJpaEntity toEntity(NotificationDelivery delivery) {
        return NotificationDeliveryJpaEntity.builder()
                .id(delivery.getId())
                .notificationId(delivery.getNotificationId())
                .outboxId(delivery.getOutboxId())
                .targetId(delivery.getTargetId())
                .userId(delivery.getUserId())
                .channel(delivery.getChannel())
                .provider(delivery.getProvider())
                .status(delivery.getStatus())
                .failureReason(delivery.getFailureReason())
                .sentAt(delivery.getSentAt())
                .build();
    }
}
