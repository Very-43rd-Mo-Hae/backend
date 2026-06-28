package com.very.relink.chat.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoomJpaEntity, Long> {
}
