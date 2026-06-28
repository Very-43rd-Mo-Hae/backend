package com.very.relink.chat.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageAttachmentJpaRepository extends JpaRepository<ChatMessageAttachmentJpaEntity, Long> {
}
