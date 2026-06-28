package com.very.relink.chat.infrastructure.persistence.jpa;

import com.very.relink.chat.domain.ChatEnums.ParticipantStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantJpaRepository extends JpaRepository<ChatParticipantJpaEntity, Long> {

    Optional<ChatParticipantJpaEntity> findByRoomIdAndMemberId(Long roomId, Long memberId);

    boolean existsByRoomIdAndMemberIdAndStatus(Long roomId, Long memberId, ParticipantStatus status);
}
