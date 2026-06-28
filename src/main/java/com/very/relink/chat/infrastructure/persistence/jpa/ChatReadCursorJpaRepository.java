package com.very.relink.chat.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatReadCursorJpaRepository extends JpaRepository<ChatReadCursorJpaEntity, Long> {

    Optional<ChatReadCursorJpaEntity> findByRoomIdAndMemberId(Long roomId, Long memberId);
}
