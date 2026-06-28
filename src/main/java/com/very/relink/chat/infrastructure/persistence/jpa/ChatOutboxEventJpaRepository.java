package com.very.relink.chat.infrastructure.persistence.jpa;

import com.very.relink.chat.domain.ChatEnums.OutboxStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatOutboxEventJpaRepository extends JpaRepository<ChatOutboxEventJpaEntity, Long> {

    List<ChatOutboxEventJpaEntity> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
