package com.very.relink.chat.application.service;

import com.very.relink.chat.application.response.ChatResponses.ChatMessageCreatedPayload;
import com.very.relink.chat.domain.ChatEnums.MessageType;
import com.very.relink.chat.domain.ChatEnums.ParticipantStatus;
import com.very.relink.chat.domain.ChatEnums.RoomType;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatRoomJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatRoomJpaRepository;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import com.very.relink.member.adapter.out.persistence.MemberJpaRepository;
import com.very.relink.notification.application.command.SendWebPushNotificationCommand;
import com.very.relink.notification.application.service.WebPushNotificationService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageNotificationService {

    private static final int MAX_BODY_LENGTH = 80;

    private final ChatParticipantJpaRepository chatParticipantJpaRepository;
    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final WebPushNotificationService webPushNotificationService;

    @Transactional
    public void notifyMessageCreated(ChatMessageCreatedPayload payload) {
        if (payload.messageType() == MessageType.SYSTEM) {
            return;
        }

        List<Long> recipientIds = chatParticipantJpaRepository
                .findByRoomIdAndStatus(payload.roomId(), ParticipantStatus.ACTIVE)
                .stream()
                .map(ChatParticipantJpaEntity::getMemberId)
                .filter(memberId -> !memberId.equals(payload.senderId()))
                .distinct()
                .toList();

        if (recipientIds.isEmpty()) {
            return;
        }

        ChatRoomJpaEntity room = chatRoomJpaRepository.findById(payload.roomId()).orElse(null);
        String senderName = memberJpaRepository.findById(payload.senderId())
                .map(MemberJpaEntity::getName)
                .filter(name -> !name.isBlank())
                .orElse("새 메시지");
        String title = createTitle(room, senderName);
        String body = createBody(payload);
        String linkUrl = "/chat/rooms/" + payload.roomId();

        recipientIds.forEach(recipientId -> webPushNotificationService.send(new SendWebPushNotificationCommand(
                recipientId,
                title,
                body,
                linkUrl,
                payload.messageId(),
                Map.of(
                        "type", "CHAT_MESSAGE_CREATED",
                        "roomId", payload.roomId(),
                        "messageId", payload.messageId(),
                        "senderId", payload.senderId()
                )
        )));
    }

    private String createTitle(ChatRoomJpaEntity room, String senderName) {
        if (room != null && room.getRoomType() == RoomType.GROUP && room.getTitle() != null && !room.getTitle().isBlank()) {
            return room.getTitle();
        }
        return senderName;
    }

    private String createBody(ChatMessageCreatedPayload payload) {
        if (payload.messageType() == MessageType.IMAGE) {
            return payload.textContent() == null || payload.textContent().isBlank()
                    ? "사진을 보냈습니다."
                    : truncate(payload.textContent());
        }

        return truncate(payload.textContent() == null ? "새 메시지가 도착했습니다." : payload.textContent());
    }

    private String truncate(String value) {
        if (value.length() <= MAX_BODY_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_BODY_LENGTH) + "...";
    }
}
