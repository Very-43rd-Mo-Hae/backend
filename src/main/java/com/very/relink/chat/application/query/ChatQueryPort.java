package com.very.relink.chat.application.query;

import com.very.relink.chat.domain.ChatEnums.AttachmentType;
import com.very.relink.chat.domain.ChatEnums.MessageStatus;
import com.very.relink.chat.domain.ChatEnums.MessageType;
import com.very.relink.chat.domain.ChatEnums.RoomType;
import java.time.LocalDateTime;
import java.util.List;

public interface ChatQueryPort {

    List<ChatRoomSummaryProjection> findRooms(Long memberId);

    List<ChatMessageProjection> findMessages(Long roomId, Long cursor, int limit);

    List<ChatMessageAttachmentProjection> findAttachments(List<Long> messageIds);

    record ChatRoomSummaryProjection(
            Long roomId,
            RoomType roomType,
            String title,
            String displayName,
            String coverImageKey,
            String lastMessage,
            MessageType lastMessageType,
            LocalDateTime lastMessageAt,
            long unreadCount
    ) {
    }

    record ChatMessageProjection(
            Long messageId,
            Long roomId,
            Long senderId,
            MessageType messageType,
            String textContent,
            MessageStatus status,
            LocalDateTime createdAt
    ) {
    }

    record ChatMessageAttachmentProjection(
            Long attachmentId,
            Long messageId,
            AttachmentType attachmentType,
            String storageKey,
            String contentType,
            Long fileSize,
            Integer width,
            Integer height,
            Integer sortOrder
    ) {
    }
}
