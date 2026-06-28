package com.very.relink.chat.infrastructure.outbox;

import com.very.relink.chat.domain.ChatEnums.OutboxStatus;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatOutboxEventJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatOutboxEventJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatOutboxRelay {

    private final ChatOutboxEventJpaRepository chatOutboxEventJpaRepository;

    public List<ChatOutboxEventJpaEntity> findPendingEvents() {
        return chatOutboxEventJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
    }
}
