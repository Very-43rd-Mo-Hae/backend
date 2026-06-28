package com.very.relink.chat.application.service;

import com.very.relink.chat.application.port.ChatPorts.StorageUrlResolver;
import com.very.relink.chat.application.query.ChatQueryPort;
import com.very.relink.chat.application.response.ChatResponses.ChatMessageAttachmentResponse;
import com.very.relink.chat.application.response.ChatResponses.ChatMessageResponse;
import com.very.relink.chat.application.response.ChatResponses.ChatMessagesResponse;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetChatMessagesQueryService {

    private final ChatParticipantJpaRepository chatParticipantJpaRepository;
    private final ChatQueryPort chatQueryPort;
    private final StorageUrlResolver storageUrlResolver;

    @Transactional(readOnly = true)
    public ChatMessagesResponse getMessages(Long memberId, Long roomId, Long cursor, int size) {
        ChatValidationSupport.requireActiveParticipant(chatParticipantJpaRepository, roomId, memberId);
        int normalizedSize = Math.max(1, Math.min(size, 100));
        List<ChatQueryPort.ChatMessageProjection> fetched = chatQueryPort.findMessages(roomId, cursor, normalizedSize + 1);
        boolean hasNext = fetched.size() > normalizedSize;
        List<ChatQueryPort.ChatMessageProjection> page = fetched.stream().limit(normalizedSize).toList();
        List<Long> messageIds = page.stream().map(ChatQueryPort.ChatMessageProjection::messageId).toList();
        Map<Long, List<ChatMessageAttachmentResponse>> attachmentsByMessageId = chatQueryPort.findAttachments(messageIds).stream()
                .map(attachment -> Map.entry(attachment.messageId(), new ChatMessageAttachmentResponse(
                        attachment.attachmentId(),
                        attachment.attachmentType().name(),
                        storageUrlResolver.resolveUrl(attachment.storageKey()),
                        attachment.storageKey(),
                        attachment.contentType(),
                        attachment.fileSize(),
                        attachment.width(),
                        attachment.height(),
                        attachment.sortOrder()
                )))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        List<ChatMessageResponse> messages = page.stream()
                .sorted(Comparator.comparing(ChatQueryPort.ChatMessageProjection::messageId))
                .map(message -> new ChatMessageResponse(
                        message.messageId(),
                        message.roomId(),
                        message.senderId(),
                        message.messageType(),
                        message.textContent(),
                        message.status(),
                        message.createdAt(),
                        attachmentsByMessageId.getOrDefault(message.messageId(), List.of())
                ))
                .toList();
        Long nextCursor = hasNext && !page.isEmpty() ? page.get(page.size() - 1).messageId() : null;
        return new ChatMessagesResponse(messages, nextCursor, hasNext);
    }
}
