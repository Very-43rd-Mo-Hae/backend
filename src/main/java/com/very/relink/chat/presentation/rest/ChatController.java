package com.very.relink.chat.presentation.rest;

import com.very.relink.chat.application.command.ChatCommands.CreateDirectChatRoomCommand;
import com.very.relink.chat.application.command.ChatCommands.CreateGroupChatRoomCommand;
import com.very.relink.chat.application.command.ChatCommands.IssueChatAttachmentPresignedUrlCommand;
import com.very.relink.chat.application.command.ChatCommands.MarkChatRoomAsReadCommand;
import com.very.relink.chat.application.command.ChatCommands.SendChatMessageAttachmentCommand;
import com.very.relink.chat.application.command.ChatCommands.SendChatMessageCommand;
import com.very.relink.chat.application.response.ChatResponses.ChatMessagesResponse;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomsResponse;
import com.very.relink.chat.application.response.ChatResponses.CreateChatRoomResponse;
import com.very.relink.chat.application.response.ChatResponses.IssueChatAttachmentPresignedUrlResponse;
import com.very.relink.chat.application.response.ChatResponses.MarkChatRoomAsReadResponse;
import com.very.relink.chat.application.response.ChatResponses.SendChatMessageResponse;
import com.very.relink.chat.application.service.CreateDirectChatRoomService;
import com.very.relink.chat.application.service.CreateGroupChatRoomService;
import com.very.relink.chat.application.service.GetChatMessagesQueryService;
import com.very.relink.chat.application.service.GetChatRoomsQueryService;
import com.very.relink.chat.application.service.IssueChatAttachmentPresignedUrlService;
import com.very.relink.chat.application.service.MarkChatRoomAsReadService;
import com.very.relink.chat.application.service.SendChatMessageService;
import com.very.relink.chat.domain.ChatEnums.MessageType;
import com.very.relink.chat.presentation.swagger.ChatSwagger;
import com.very.relink.core.presentation.RestResponse;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController implements ChatSwagger {

    private final CurrentUserProvider currentUserProvider;
    private final CreateDirectChatRoomService createDirectChatRoomService;
    private final CreateGroupChatRoomService createGroupChatRoomService;
    private final GetChatRoomsQueryService getChatRoomsQueryService;
    private final GetChatMessagesQueryService getChatMessagesQueryService;
    private final SendChatMessageService sendChatMessageService;
    private final MarkChatRoomAsReadService markChatRoomAsReadService;
    private final IssueChatAttachmentPresignedUrlService issueChatAttachmentPresignedUrlService;

    @PostMapping("/rooms/direct")
    @Override
    public ResponseEntity<RestResponse<CreateChatRoomResponse>> createDirectRoom(@Valid @RequestBody CreateDirectChatRoomRequest request) {
        Long requesterId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(createDirectChatRoomService.create(
                new CreateDirectChatRoomCommand(requesterId, request.targetMemberId())
        )));
    }

    @PostMapping("/rooms/group")
    @Override
    public ResponseEntity<RestResponse<CreateChatRoomResponse>> createGroupRoom(@Valid @RequestBody CreateGroupChatRoomRequest request) {
        Long requesterId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(createGroupChatRoomService.create(
                new CreateGroupChatRoomCommand(requesterId, request.title(), request.participantMemberIds(), request.coverImageKey())
        )));
    }

    @GetMapping("/rooms")
    @Override
    public ResponseEntity<RestResponse<ChatRoomsResponse>> getRooms() {
        return ResponseEntity.ok(new RestResponse<>(getChatRoomsQueryService.getRooms(currentUserProvider.getCurrentUserId())));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Override
    public ResponseEntity<RestResponse<ChatMessagesResponse>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "30") int size
    ) {
        Long requesterId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(getChatMessagesQueryService.getMessages(requesterId, roomId, cursor, size)));
    }

    @PostMapping("/rooms/{roomId}/messages")
    @Override
    public ResponseEntity<RestResponse<SendChatMessageResponse>> sendMessage(
            @PathVariable Long roomId,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        Long requesterId = currentUserProvider.getCurrentUserId();
        List<SendChatMessageAttachmentCommand> attachments = request.attachments() == null
                ? List.of()
                : request.attachments().stream()
                .map(attachment -> new SendChatMessageAttachmentCommand(
                        attachment.storageKey(),
                        attachment.originalFileName(),
                        attachment.contentType(),
                        attachment.fileSize(),
                        attachment.width(),
                        attachment.height(),
                        attachment.sortOrder()
                ))
                .toList();
        return ResponseEntity.ok(new RestResponse<>(sendChatMessageService.send(new SendChatMessageCommand(
                requesterId,
                roomId,
                request.clientMessageId(),
                request.messageType(),
                request.textContent(),
                attachments
        ))));
    }

    @PatchMapping("/rooms/{roomId}/read")
    @Override
    public ResponseEntity<RestResponse<MarkChatRoomAsReadResponse>> markAsRead(
            @PathVariable Long roomId,
            @Valid @RequestBody MarkChatRoomAsReadRequest request
    ) {
        Long requesterId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(markChatRoomAsReadService.markAsRead(
                new MarkChatRoomAsReadCommand(requesterId, roomId, request.lastReadMessageId())
        )));
    }

    @PostMapping("/attachments/presigned-url")
    @Override
    public ResponseEntity<RestResponse<IssueChatAttachmentPresignedUrlResponse>> issuePresignedUrl(
            @Valid @RequestBody IssueChatAttachmentPresignedUrlRequest request
    ) {
        Long requesterId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(issueChatAttachmentPresignedUrlService.issue(
                new IssueChatAttachmentPresignedUrlCommand(requesterId, request.roomId(), request.fileName(), request.contentType(), request.fileSize())
        )));
    }

    public record CreateDirectChatRoomRequest(@NotNull Long targetMemberId) {
    }

    public record CreateGroupChatRoomRequest(
            @NotBlank String title,
            @NotEmpty List<Long> participantMemberIds,
            String coverImageKey
    ) {
    }

    public record SendChatMessageRequest(
            @NotBlank String clientMessageId,
            @NotNull MessageType messageType,
            String textContent,
            List<@Valid SendChatMessageAttachmentRequest> attachments
    ) {
    }

    public record SendChatMessageAttachmentRequest(
            @NotBlank String storageKey,
            @NotBlank String originalFileName,
            @NotBlank String contentType,
            @NotNull @Positive Long fileSize,
            Integer width,
            Integer height,
            Integer sortOrder
    ) {
    }

    public record MarkChatRoomAsReadRequest(@NotNull Long lastReadMessageId) {
    }

    public record IssueChatAttachmentPresignedUrlRequest(
            @NotNull Long roomId,
            @NotBlank String fileName,
            @NotBlank String contentType,
            @NotNull @Positive Long fileSize
    ) {
    }
}
