package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.notification.application.port.out.NotificationHistoryCommandPort;
import com.very.relink.notification.domain.model.NotificationChannel;
import com.very.relink.notification.domain.model.NotificationDelivery;
import com.very.relink.notification.domain.model.NotificationMessage;
import com.very.relink.notification.domain.model.NotificationOutbox;
import com.very.relink.notification.domain.model.NotificationOutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Component;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
@RequiredArgsConstructor
public class NotificationHistoryPersistenceAdapter implements NotificationHistoryCommandPort {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationOutboxJpaRepository notificationOutboxJpaRepository;
    private final NotificationDeliveryJpaRepository notificationDeliveryJpaRepository;
    private final NotificationHistoryMapper notificationHistoryMapper;
    private final DSLContext dslContext;

    @Override
    public NotificationMessage saveNotification(NotificationMessage notification) {
        NotificationJpaEntity saved = notificationJpaRepository.save(notificationHistoryMapper.toEntity(notification));
        return notificationHistoryMapper.toDomain(saved);
    }

    @Override
    public NotificationOutbox saveOutbox(NotificationOutbox outbox) {
        NotificationOutboxJpaEntity saved = notificationOutboxJpaRepository.save(notificationHistoryMapper.toEntity(outbox));
        return notificationHistoryMapper.toDomain(saved);
    }

    @Override
    public List<NotificationOutbox> claimPendingOutboxes(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return dslContext.transactionResult(configuration -> {
            DSLContext transactionDsl = configuration.dsl();
            List<NotificationOutbox> outboxes = transactionDsl
                    .select(
                            field("notification_outbox_id", Long.class),
                            field("notification_id", Long.class),
                            field("user_id", Long.class),
                            field("channel", String.class),
                            field("title", String.class),
                            field("body", String.class),
                            field("link_url", String.class),
                            field("deduplication_id", Long.class),
                            field("data_json", String.class),
                            field("status", String.class),
                            field("attempt_count", Integer.class),
                            field("failure_reason", String.class)
                    )
                    .from(table("notification_outbox"))
                    .where(field("status", String.class).eq(NotificationOutboxStatus.PENDING.name()))
                    .orderBy(field("created_at").asc())
                    .limit(limit)
                    .forUpdate()
                    .fetch(this::toNotificationOutbox);

            List<Long> outboxIds = outboxes.stream()
                    .map(NotificationOutbox::getId)
                    .toList();
            if (!outboxIds.isEmpty()) {
                transactionDsl
                        .update(table("notification_outbox"))
                        .set(field("status", String.class), NotificationOutboxStatus.PROCESSING.name())
                        .set(field("attempt_count", Integer.class), field("attempt_count", Integer.class).plus(1))
                        .set(field("updated_at", LocalDateTime.class), LocalDateTime.now())
                        .where(field("notification_outbox_id", Long.class).in(outboxIds))
                        .execute();
            }

            return outboxes.stream()
                    .map(this::toProcessingOutbox)
                    .toList();
        });
    }

    @Override
    public void markOutboxSent(Long outboxId) {
        updateOutbox(outboxId, NotificationOutboxStatus.SENT, null);
    }

    @Override
    public void markOutboxSkipped(Long outboxId, String reason) {
        updateOutbox(outboxId, NotificationOutboxStatus.SKIPPED, reason);
    }

    @Override
    public void markOutboxFailed(Long outboxId, String reason) {
        updateOutbox(outboxId, NotificationOutboxStatus.FAILED, reason);
    }

    @Override
    public void saveDelivery(NotificationDelivery delivery) {
        notificationDeliveryJpaRepository.save(notificationHistoryMapper.toEntity(delivery));
    }

    private void updateOutbox(Long outboxId, NotificationOutboxStatus status, String reason) {
        dslContext
                .update(table("notification_outbox"))
                .set(field("status", String.class), status.name())
                .set(field("failure_reason", String.class), reason)
                .set(field("updated_at", LocalDateTime.class), LocalDateTime.now())
                .where(field("notification_outbox_id", Long.class).eq(outboxId))
                .execute();
    }

    private NotificationOutbox toNotificationOutbox(Record record) {
        return NotificationOutbox.builder()
                .id(record.get(field("notification_outbox_id", Long.class)))
                .notificationId(record.get(field("notification_id", Long.class)))
                .userId(record.get(field("user_id", Long.class)))
                .channel(NotificationChannel.valueOf(record.get(field("channel", String.class))))
                .title(record.get(field("title", String.class)))
                .body(record.get(field("body", String.class)))
                .linkUrl(record.get(field("link_url", String.class)))
                .deduplicationId(record.get(field("deduplication_id", Long.class)))
                .dataJson(record.get(field("data_json", String.class)))
                .status(NotificationOutboxStatus.valueOf(record.get(field("status", String.class))))
                .attemptCount(record.get(field("attempt_count", Integer.class)))
                .failureReason(record.get(field("failure_reason", String.class)))
                .build();
    }

    private NotificationOutbox toProcessingOutbox(NotificationOutbox outbox) {
        return NotificationOutbox.builder()
                .id(outbox.getId())
                .notificationId(outbox.getNotificationId())
                .userId(outbox.getUserId())
                .channel(outbox.getChannel())
                .title(outbox.getTitle())
                .body(outbox.getBody())
                .linkUrl(outbox.getLinkUrl())
                .deduplicationId(outbox.getDeduplicationId())
                .dataJson(outbox.getDataJson())
                .status(NotificationOutboxStatus.PROCESSING)
                .attemptCount(outbox.getAttemptCount() + 1)
                .failureReason(outbox.getFailureReason())
                .build();
    }
}
