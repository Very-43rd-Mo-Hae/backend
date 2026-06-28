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
        name = "direct_chat_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_direct_chat_room_members", columnNames = {"member_low_id", "member_high_id"}),
                @UniqueConstraint(name = "uk_direct_chat_room_room_id", columnNames = "room_id")
        }
)
public class DirectChatRoomJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direct_chat_room_id")
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "member_low_id", nullable = false)
    private Long memberLowId;

    @Column(name = "member_high_id", nullable = false)
    private Long memberHighId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static DirectChatRoomJpaEntity create(Long roomId, Long memberAId, Long memberBId) {
        long low = Math.min(memberAId, memberBId);
        long high = Math.max(memberAId, memberBId);
        return DirectChatRoomJpaEntity.builder()
                .roomId(roomId)
                .memberLowId(low)
                .memberHighId(high)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
