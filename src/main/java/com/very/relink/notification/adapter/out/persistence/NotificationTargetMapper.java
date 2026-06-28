package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.notification.domain.model.NotificationTarget;
import org.springframework.stereotype.Component;

@Component
public class NotificationTargetMapper {

    public NotificationTarget toDomain(NotificationTargetJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return NotificationTarget.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .channel(entity.getChannel())
                .provider(entity.getProvider())
                .endpoint(entity.getEndpoint())
                .p256dh(entity.getP256dh())
                .auth(entity.getAuth())
                .userAgent(entity.getUserAgent())
                .status(entity.getStatus())
                .lastUsedAt(entity.getLastUsedAt())
                .build();
    }

    public NotificationTargetJpaEntity toEntity(NotificationTarget target) {
        if (target == null) {
            return null;
        }

        return NotificationTargetJpaEntity.builder()
                .id(target.getId())
                .userId(target.getUserId())
                .channel(target.getChannel())
                .provider(target.getProvider())
                .endpoint(target.getEndpoint())
                .p256dh(target.getP256dh())
                .auth(target.getAuth())
                .userAgent(target.getUserAgent())
                .status(target.getStatus())
                .lastUsedAt(target.getLastUsedAt())
                .build();
    }
}
