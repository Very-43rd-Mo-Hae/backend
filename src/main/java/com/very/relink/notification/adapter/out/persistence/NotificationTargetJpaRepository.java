package com.very.relink.notification.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTargetJpaRepository extends JpaRepository<NotificationTargetJpaEntity, Long> {

    Optional<NotificationTargetJpaEntity> findByEndpoint(String endpoint);
}
