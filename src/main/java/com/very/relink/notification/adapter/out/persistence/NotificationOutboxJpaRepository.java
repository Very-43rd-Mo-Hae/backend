package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.notification.domain.model.NotificationOutboxStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxJpaRepository extends JpaRepository<NotificationOutboxJpaEntity, Long> {

    List<NotificationOutboxJpaEntity> findTop50ByStatusOrderByCreatedAtAsc(NotificationOutboxStatus status);
}
