package com.very.relink.chat.application.service;

import com.very.relink.chat.domain.ChatEnums.MessageType;
import com.very.relink.chat.domain.ChatEnums.ParticipantStatus;
import com.very.relink.chat.exception.ChatErrorCode;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaRepository;
import java.util.List;
import java.util.Set;

public final class ChatValidationSupport {

    static final long MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024L;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private ChatValidationSupport() {
    }

    static void requireActiveParticipant(ChatParticipantJpaRepository repository, Long roomId, Long memberId) {
        if (!repository.existsByRoomIdAndMemberIdAndStatus(roomId, memberId, ParticipantStatus.ACTIVE)) {
            throw ChatErrorCode.NOT_ACTIVE_CHAT_PARTICIPANT.toException();
        }
    }

    static void validateMessage(MessageType messageType, String textContent, List<?> attachments) {
        if (messageType == null) {
            throw ChatErrorCode.INVALID_MESSAGE_TYPE.toException();
        }
        int attachmentSize = attachments == null ? 0 : attachments.size();
        if (messageType == MessageType.TEXT) {
            if (textContent == null || textContent.isBlank()) {
                throw ChatErrorCode.EMPTY_TEXT_MESSAGE.toException();
            }
            if (attachmentSize > 0) {
                throw ChatErrorCode.TEXT_MESSAGE_CANNOT_HAVE_ATTACHMENT.toException();
            }
            return;
        }
        if (messageType == MessageType.IMAGE && attachmentSize == 0) {
            throw ChatErrorCode.IMAGE_MESSAGE_REQUIRES_ATTACHMENT.toException();
        }
    }

    public static void validateImageFile(String contentType, Long fileSize) {
        if (!IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw ChatErrorCode.UNSUPPORTED_ATTACHMENT_CONTENT_TYPE.toException();
        }
        if (fileSize == null || fileSize <= 0 || fileSize > MAX_IMAGE_FILE_SIZE) {
            throw ChatErrorCode.ATTACHMENT_FILE_TOO_LARGE.toException();
        }
    }
}
