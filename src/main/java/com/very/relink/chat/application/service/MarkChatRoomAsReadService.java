package com.very.relink.chat.application.service;

import com.very.relink.chat.application.command.ChatCommands.MarkChatRoomAsReadCommand;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomReadPayload;
import com.very.relink.chat.application.response.ChatResponses.MarkChatRoomAsReadResponse;
import com.very.relink.chat.domain.ChatEnums.OutboxEventType;
import com.very.relink.chat.exception.ChatErrorCode;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatMessageJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatOutboxEventJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatOutboxEventJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatReadCursorJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatReadCursorJpaRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class MarkChatRoomAsReadService {

    private final ChatParticipantJpaRepository chatParticipantJpaRepository;
    private final ChatMessageJpaRepository chatMessageJpaRepository;
    private final ChatReadCursorJpaRepository chatReadCursorJpaRepository;
    private final ChatOutboxEventJpaRepository chatOutboxEventJpaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public MarkChatRoomAsReadResponse markAsRead(MarkChatRoomAsReadCommand command) {
        ChatValidationSupport.requireActiveParticipant(chatParticipantJpaRepository, command.roomId(), command.requesterId());
        chatMessageJpaRepository.findByIdAndRoomId(command.lastReadMessageId(), command.roomId())
                .orElseThrow(ChatErrorCode.INVALID_READ_CURSOR::toException);

        LocalDateTime readAt = LocalDateTime.now();
        ChatReadCursorJpaEntity cursor = chatReadCursorJpaRepository.findByRoomIdAndMemberId(command.roomId(), command.requesterId())
                .map(existing -> {
                    existing.advanceTo(command.lastReadMessageId(), readAt);
                    return existing;
                })
                .orElseGet(() -> ChatReadCursorJpaEntity.builder()
                        .roomId(command.roomId())
                        .memberId(command.requesterId())
                        .lastReadMessageId(command.lastReadMessageId())
                        .lastReadAt(readAt)
                        .build());

        ChatReadCursorJpaEntity saved = chatReadCursorJpaRepository.save(cursor);
        ChatRoomReadPayload payload = new ChatRoomReadPayload(saved.getRoomId(), saved.getMemberId(), saved.getLastReadMessageId(), saved.getLastReadAt());
        chatOutboxEventJpaRepository.save(ChatOutboxEventJpaEntity.pending(
                "ChatRoom",
                saved.getRoomId(),
                OutboxEventType.CHAT_ROOM_READ,
                toJson(payload)
        ));
        return new MarkChatRoomAsReadResponse(saved.getRoomId(), saved.getMemberId(), saved.getLastReadMessageId(), saved.getLastReadAt());
    }

    private String toJson(ChatRoomReadPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalStateException("채팅 읽음 outbox payload 직렬화에 실패했습니다.", ex);
        }
    }
}
