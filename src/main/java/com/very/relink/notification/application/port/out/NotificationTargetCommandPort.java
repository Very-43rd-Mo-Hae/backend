package com.very.relink.notification.application.port.out;

import com.very.relink.notification.domain.model.NotificationTarget;
import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationTargetCommandPort {

    Optional<NotificationTarget> findByEndpoint(String endpoint);

    NotificationTarget save(NotificationTarget target);

    void disableByEndpoint(Long userId, String endpoint);

    void expireByEndpoint(String endpoint);

    void markUsed(Long targetId, LocalDateTime usedAt);
}
