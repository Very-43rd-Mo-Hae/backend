package com.very.relink.chat.infrastructure.persistence.jpa;

import com.very.relink.chat.domain.ChatEnums.ParticipantRole;
import com.very.relink.chat.domain.ChatEnums.ParticipantStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
        name = "chat_participant",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_participant_room_member", columnNames = {"room_id", "member_id"}),
        indexes = @Index(name = "idx_chat_participant_member_status", columnList = "member_id,status")
)
public class ChatParticipantJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_participant_id")
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ParticipantRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParticipantStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    public static ChatParticipantJpaEntity active(Long roomId, Long memberId, ParticipantRole role) {
        return ChatParticipantJpaEntity.builder()
                .roomId(roomId)
                .memberId(memberId)
                .role(role)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
