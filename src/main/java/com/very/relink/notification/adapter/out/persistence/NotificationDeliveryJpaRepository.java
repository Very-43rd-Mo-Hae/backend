package com.very.relink.notification.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeliveryJpaRepository extends JpaRepository<NotificationDeliveryJpaEntity, Long> {
}
