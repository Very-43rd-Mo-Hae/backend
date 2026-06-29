package com.very.relink.chat.application.service;

import com.very.relink.chat.application.command.ChatCommands.SendChatMessageAttachmentCommand;
import com.very.relink.chat.application.command.ChatCommands.SendChatMessageCommand;
import com.very.relink.chat.application.response.ChatResponses.ChatMessageAttachmentResponse;
import com.very.relink.chat.application.response.ChatResponses.ChatMessageCreatedPayload;
import com.very.relink.chat.application.response.ChatResponses.SendChatMessageResponse;
import com.very.relink.chat.domain.ChatEnums.AttachmentType;
import com.very.relink.chat.domain.ChatEnums.OutboxEventType;
import com.very.relink.chat.domain.ChatEnums.ParticipantStatus;
import com.very.relink.chat.exception.ChatErrorCode;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatMessageAttachmentJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatMessageAttachmentJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatMessageJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatMessageJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatOutboxEventJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatOutboxEventJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class SendChatMessageService {

    private final ChatParticipantJpaRepository chatParticipantJpaRepository;
    private final ChatMessageJpaRepository chatMessageJpaRepository;
    private final ChatMessageAttachmentJpaRepository chatMessageAttachmentJpaRepository;
    private final ChatOutboxEventJpaRepository chatOutboxEventJpaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SendChatMessageResponse send(SendChatMessageCommand command) {
        if (!chatParticipantJpaRepository.existsByRoomIdAndMemberIdAndStatus(command.roomId(), command.requesterId(), ParticipantStatus.ACTIVE)) {
            throw ChatErrorCode.NOT_ACTIVE_CHAT_PARTICIPANT.toException();
        }
        List<SendChatMessageAttachmentCommand> attachments = command.attachments() == null ? List.of() : command.attachments();
        ChatValidationSupport.validateMessage(command.messageType(), command.textContent(), attachments);
        attachments.forEach(attachment -> ChatValidationSupport.validateImageFile(attachment.contentType(), attachment.fileSize()));

        if (chatMessageJpaRepository.findByRoomIdAndSenderIdAndClientMessageId(command.roomId(), command.requesterId(), command.clientMessageId()).isPresent()) {
            throw ChatErrorCode.DUPLICATED_CLIENT_MESSAGE_ID.toException();
        }

        try {
            ChatMessageJpaEntity message = chatMessageJpaRepository.save(ChatMessageJpaEntity.create(
                    command.roomId(),
                    command.requesterId(),
                    command.messageType(),
                    command.textContent(),
                    command.clientMessageId()
            ));
            List<ChatMessageAttachmentResponse> savedAttachments = saveAttachments(message.getId(), attachments);
            ChatMessageCreatedPayload payload = new ChatMessageCreatedPayload(
                    message.getId(),
                    message.getRoomId(),
                    message.getSenderId(),
                    message.getMessageType(),
                    message.getTextContent(),
                    savedAttachments,
                    message.getCreatedAt()
            );
            chatOutboxEventJpaRepository.save(ChatOutboxEventJpaEntity.pending(
                    "ChatMessage",
                    message.getId(),
                    OutboxEventType.CHAT_MESSAGE_CREATED,
                    toJson(payload)
            ));
            return new SendChatMessageResponse(
                    message.getId(),
                    message.getRoomId(),
                    message.getSenderId(),
                    message.getMessageType(),
                    message.getTextContent(),
                    message.getCreatedAt()
            );
        } catch (DataIntegrityViolationException ex) {
            throw ChatErrorCode.DUPLICATED_CLIENT_MESSAGE_ID.toException();
        }
    }

    private List<ChatMessageAttachmentResponse> saveAttachments(Long messageId, List<SendChatMessageAttachmentCommand> attachments) {
        return attachments.stream()
                .map(attachment -> chatMessageAttachmentJpaRepository.save(ChatMessageAttachmentJpaEntity.builder()
                        .messageId(messageId)
                        .attachmentType(AttachmentType.IMAGE)
                        .originalFileName(attachment.originalFileName())
                        .contentType(attachment.contentType())
                        .fileSize(attachment.fileSize())
                        .storageKey(attachment.storageKey())
                        .width(attachment.width())
                        .height(attachment.height())
                        .sortOrder(attachment.sortOrder() == null ? 0 : attachment.sortOrder())
                        .createdAt(LocalDateTime.now())
                        .build()))
                .map(entity -> new ChatMessageAttachmentResponse(
                        entity.getId(),
                        entity.getAttachmentType().name(),
                        null,
                        entity.getStorageKey(),
                        entity.getContentType(),
                        entity.getFileSize(),
                        entity.getWidth(),
                        entity.getHeight(),
                        entity.getSortOrder()
                ))
                .toList();
    }

    private String toJson(ChatMessageCreatedPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalStateException("채팅 메시지 outbox payload 직렬화에 실패했습니다.", ex);
        }
    }
}
