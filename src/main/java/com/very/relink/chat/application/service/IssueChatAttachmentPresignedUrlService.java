package com.very.relink.chat.application.service;

import com.very.relink.chat.application.command.ChatCommands.IssueChatAttachmentPresignedUrlCommand;
import com.very.relink.chat.application.port.ChatPorts.StoragePresignedUrlPort;
import com.very.relink.chat.application.response.ChatResponses.IssueChatAttachmentPresignedUrlResponse;
import com.very.relink.chat.application.response.ChatResponses.PresignedUploadUrl;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueChatAttachmentPresignedUrlService {

    private final ChatParticipantJpaRepository chatParticipantJpaRepository;
    private final StoragePresignedUrlPort storagePresignedUrlPort;

    @Transactional(readOnly = true)
    public IssueChatAttachmentPresignedUrlResponse issue(IssueChatAttachmentPresignedUrlCommand command) {
        ChatValidationSupport.requireActiveParticipant(chatParticipantJpaRepository, command.roomId(), command.requesterId());
        ChatValidationSupport.validateImageFile(command.contentType(), command.fileSize());
        PresignedUploadUrl presignedUploadUrl = storagePresignedUrlPort.issueUploadUrl(command);
        return new IssueChatAttachmentPresignedUrlResponse(
                presignedUploadUrl.uploadUrl(),
                presignedUploadUrl.storageKey(),
                presignedUploadUrl.expiresIn()
        );
    }
}
