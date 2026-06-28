package com.very.relink.chat.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageJpaEntity, Long> {

    Optional<ChatMessageJpaEntity> findByRoomIdAndSenderIdAndClientMessageId(Long roomId, Long senderId, String clientMessageId);

    Optional<ChatMessageJpaEntity> findByIdAndRoomId(Long id, Long roomId);
}
