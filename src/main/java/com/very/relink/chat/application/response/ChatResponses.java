package com.very.relink.chat.application.response;

import com.very.relink.chat.domain.ChatEnums.MessageStatus;
import com.very.relink.chat.domain.ChatEnums.MessageType;
import com.very.relink.chat.domain.ChatEnums.RoomType;
import java.time.LocalDateTime;
import java.util.List;

public final class ChatResponses {

    private ChatResponses() {
    }

    public record CreateChatRoomResponse(Long roomId, RoomType roomType) {
    }

    public record ChatRoomSummaryResponse(
            Long roomId,
            RoomType roomType,
            String title,
            String displayName,
            String coverImageUrl,
            String lastMessage,
            MessageType lastMessageType,
            LocalDateTime lastMessageAt,
            long unreadCount
    ) {
    }

    public record ChatRoomsResponse(List<ChatRoomSummaryResponse> rooms) {
    }

    public record ChatMessageAttachmentResponse(
            Long attachmentId,
            String attachmentType,
            String imageUrl,
            String storageKey,
            String contentType,
            Long fileSize,
            Integer width,
            Integer height,
            Integer sortOrder
    ) {
    }

    public record ChatMessageResponse(
            Long messageId,
            Long roomId,
            Long senderId,
            MessageType messageType,
            String textContent,
            MessageStatus status,
            LocalDateTime createdAt,
            List<ChatMessageAttachmentResponse> attachments
    ) {
    }

    public record ChatMessagesResponse(List<ChatMessageResponse> messages, Long nextCursor, boolean hasNext) {
    }

    public record SendChatMessageResponse(
            Long messageId,
            Long roomId,
            Long senderId,
            MessageType messageType,
            String textContent,
            LocalDateTime createdAt
    ) {
    }

    public record MarkChatRoomAsReadResponse(Long roomId, Long memberId, Long lastReadMessageId, LocalDateTime lastReadAt) {
    }

    public record IssueChatAttachmentPresignedUrlResponse(String uploadUrl, String storageKey, long expiresIn) {
    }

    public record PresignedUploadUrl(String uploadUrl, String storageKey, long expiresIn) {
    }

    public record ChatMessageCreatedPayload(
            Long messageId,
            Long roomId,
            Long senderId,
            MessageType messageType,
            String textContent,
            List<ChatMessageAttachmentResponse> attachments,
            LocalDateTime createdAt
    ) {
    }

    public record ChatRoomReadPayload(Long roomId, Long memberId, Long lastReadMessageId, LocalDateTime lastReadAt) {
    }
}
