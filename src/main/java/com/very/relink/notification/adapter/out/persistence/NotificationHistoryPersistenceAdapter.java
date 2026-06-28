package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.notification.application.port.out.NotificationHistoryCommandPort;
import com.very.relink.notification.domain.model.NotificationDelivery;
import com.very.relink.notification.domain.model.NotificationMessage;
import com.very.relink.notification.domain.model.NotificationOutbox;
import com.very.relink.notification.domain.model.NotificationOutboxStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationHistoryPersistenceAdapter implements NotificationHistoryCommandPort {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationOutboxJpaRepository notificationOutboxJpaRepository;
    private final NotificationDeliveryJpaRepository notificationDeliveryJpaRepository;
    private final NotificationHistoryMapper notificationHistoryMapper;

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
        return notificationOutboxJpaRepository.findTop50ByStatusOrderByCreatedAtAsc(NotificationOutboxStatus.PENDING)
                .stream()
                .limit(limit)
                .peek(entity -> {
                    entity.setStatus(NotificationOutboxStatus.PROCESSING);
                    entity.setAttemptCount(entity.getAttemptCount() + 1);
                })
                .map(notificationOutboxJpaRepository::save)
                .map(notificationHistoryMapper::toDomain)
                .toList();
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
        notificationOutboxJpaRepository.findById(outboxId)
                .ifPresent(outbox -> {
                    outbox.setStatus(status);
                    outbox.setFailureReason(reason);
                    notificationOutboxJpaRepository.save(outbox);
                });
    }
}
