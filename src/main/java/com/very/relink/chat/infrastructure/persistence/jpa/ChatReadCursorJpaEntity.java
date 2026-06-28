package com.very.relink.chat.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        name = "chat_read_cursor",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_read_cursor_room_member", columnNames = {"room_id", "member_id"})
)
public class ChatReadCursorJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_read_cursor_id")
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "last_read_message_id", nullable = false)
    private Long lastReadMessageId;

    @Column(name = "last_read_at", nullable = false)
    private LocalDateTime lastReadAt;

    public void advanceTo(Long messageId, LocalDateTime readAt) {
        if (messageId > this.lastReadMessageId) {
            this.lastReadMessageId = messageId;
            this.lastReadAt = readAt;
        }
    }
}
