package com.very.relink.chat.application.command;

import com.very.relink.chat.domain.ChatEnums.MessageType;
import java.util.List;

public final class ChatCommands {

    private ChatCommands() {
    }

    public record CreateDirectChatRoomCommand(Long requesterId, Long targetMemberId) {
    }

    public record CreateGroupChatRoomCommand(Long requesterId, String title, List<Long> participantMemberIds, String coverImageKey) {
    }

    public record SendChatMessageCommand(
            Long requesterId,
            Long roomId,
            String clientMessageId,
            MessageType messageType,
            String textContent,
            List<SendChatMessageAttachmentCommand> attachments
    ) {
    }

    public record SendChatMessageAttachmentCommand(
            String storageKey,
            String originalFileName,
            String contentType,
            Long fileSize,
            Integer width,
            Integer height,
            Integer sortOrder
    ) {
    }

    public record MarkChatRoomAsReadCommand(Long requesterId, Long roomId, Long lastReadMessageId) {
    }

    public record IssueChatAttachmentPresignedUrlCommand(Long requesterId, Long roomId, String fileName, String contentType, Long fileSize) {
    }

    public record IssueProfileImagePresignedUrlCommand(Long requesterId, String fileName, String contentType, Long fileSize) {
    }
}
