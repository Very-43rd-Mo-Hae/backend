package com.very.relink.chat.infrastructure.persistence.jpa;

import com.very.relink.chat.domain.ChatEnums.RoomStatus;
import com.very.relink.chat.domain.ChatEnums.RoomType;
import com.very.relink.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room")
public class ChatRoomJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "cover_image_key", length = 512)
    private String coverImageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoomStatus status;

    public static ChatRoomJpaEntity createDirect() {
        return ChatRoomJpaEntity.builder()
                .roomType(RoomType.DIRECT)
                .status(RoomStatus.ACTIVE)
                .build();
    }

    public static ChatRoomJpaEntity createGroup(String title, String coverImageKey) {
        return ChatRoomJpaEntity.builder()
                .roomType(RoomType.GROUP)
                .title(title)
                .coverImageKey(coverImageKey)
                .status(RoomStatus.ACTIVE)
                .build();
    }

    public static ChatRoomJpaEntity createAppointment(String title) {
        return ChatRoomJpaEntity.builder()
                .roomType(RoomType.APPOINTMENT)
                .title(title)
                .status(RoomStatus.ACTIVE)
                .build();
    }
}
