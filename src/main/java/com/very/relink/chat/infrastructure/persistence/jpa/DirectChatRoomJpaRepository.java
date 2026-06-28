package com.very.relink.chat.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectChatRoomJpaRepository extends JpaRepository<DirectChatRoomJpaEntity, Long> {

    Optional<DirectChatRoomJpaEntity> findByMemberLowIdAndMemberHighId(Long memberLowId, Long memberHighId);
}
