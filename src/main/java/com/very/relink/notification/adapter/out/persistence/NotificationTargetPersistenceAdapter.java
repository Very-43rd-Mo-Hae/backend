package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.notification.application.port.out.NotificationTargetCommandPort;
import com.very.relink.notification.domain.model.NotificationTarget;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationTargetPersistenceAdapter implements NotificationTargetCommandPort {

    private final NotificationTargetJpaRepository notificationTargetJpaRepository;
    private final NotificationTargetMapper notificationTargetMapper;

    @Override
    public Optional<NotificationTarget> findByEndpoint(String endpoint) {
        return notificationTargetJpaRepository.findByEndpoint(endpoint)
                .map(notificationTargetMapper::toDomain);
    }

    @Override
    public NotificationTarget save(NotificationTarget target) {
        NotificationTargetJpaEntity entity = notificationTargetMapper.toEntity(target);
        NotificationTargetJpaEntity savedEntity = notificationTargetJpaRepository.save(entity);
        return notificationTargetMapper.toDomain(savedEntity);
    }

    @Override
    public void disableByEndpoint(Long userId, String endpoint) {
        notificationTargetJpaRepository.findByEndpoint(endpoint)
                .map(notificationTargetMapper::toDomain)
                .filter(target -> target.getUserId().equals(userId))
                .ifPresent(target -> {
                    target.disable();
                    save(target);
                });
    }

    @Override
    public void expireByEndpoint(String endpoint) {
        notificationTargetJpaRepository.findByEndpoint(endpoint)
                .map(notificationTargetMapper::toDomain)
                .ifPresent(target -> {
                    target.expire();
                    save(target);
                });
    }

    @Override
    public void markUsed(Long targetId, LocalDateTime usedAt) {
        notificationTargetJpaRepository.findById(targetId)
                .map(notificationTargetMapper::toDomain)
                .ifPresent(target -> {
                    target.markUsed(usedAt);
                    save(target);
                });
    }
}
