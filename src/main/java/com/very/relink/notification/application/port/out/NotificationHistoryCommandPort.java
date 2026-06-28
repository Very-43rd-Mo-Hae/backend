package com.very.relink.notification.application.port.out;

import com.very.relink.notification.domain.model.NotificationDelivery;
import com.very.relink.notification.domain.model.NotificationMessage;
import com.very.relink.notification.domain.model.NotificationOutbox;
import java.util.List;

public interface NotificationHistoryCommandPort {

    NotificationMessage saveNotification(NotificationMessage notification);

    NotificationOutbox saveOutbox(NotificationOutbox outbox);

    List<NotificationOutbox> claimPendingOutboxes(int limit);

    void markOutboxSent(Long outboxId);

    void markOutboxSkipped(Long outboxId, String reason);

    void markOutboxFailed(Long outboxId, String reason);

    void saveDelivery(NotificationDelivery delivery);
}
