package com.very.relink.chat.application.port;

import com.very.relink.chat.application.command.ChatCommands.IssueChatAttachmentPresignedUrlCommand;
import com.very.relink.chat.application.command.ChatCommands.IssueProfileImagePresignedUrlCommand;
import com.very.relink.chat.application.response.ChatResponses.ChatMessageCreatedPayload;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomReadPayload;
import com.very.relink.chat.application.response.ChatResponses.PresignedUploadUrl;

public final class ChatPorts {

    private ChatPorts() {
    }

    public interface ChatMessagePublisher {
        void publishMessageCreated(ChatMessageCreatedPayload payload);

        void publishRoomRead(ChatRoomReadPayload payload);
    }

    public interface StoragePresignedUrlPort {
        PresignedUploadUrl issueUploadUrl(IssueChatAttachmentPresignedUrlCommand command);

        PresignedUploadUrl issueProfileImageUploadUrl(IssueProfileImagePresignedUrlCommand command);
    }

    public interface StorageUrlResolver {
        String resolveUrl(String storageKey);
    }
}
