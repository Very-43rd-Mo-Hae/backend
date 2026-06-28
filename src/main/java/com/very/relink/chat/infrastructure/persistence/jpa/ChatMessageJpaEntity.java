package com.very.relink.chat.infrastructure.persistence.jpa;

import com.very.relink.chat.domain.ChatEnums.MessageStatus;
import com.very.relink.chat.domain.ChatEnums.MessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_message",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_message_client_id", columnNames = {"room_id", "sender_id", "client_message_id"}),
        indexes = @Index(name = "idx_chat_message_room_id_id", columnList = "room_id,chat_message_id")
)
public class ChatMessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Lob
    @Column(name = "text_content")
    private String textContent;

    @Column(name = "client_message_id", nullable = false, length = 100)
    private String clientMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static ChatMessageJpaEntity create(Long roomId, Long senderId, MessageType messageType, String textContent, String clientMessageId) {
        return ChatMessageJpaEntity.builder()
                .roomId(roomId)
                .senderId(senderId)
                .messageType(messageType)
                .textContent(textContent)
                .clientMessageId(clientMessageId)
                .status(MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
