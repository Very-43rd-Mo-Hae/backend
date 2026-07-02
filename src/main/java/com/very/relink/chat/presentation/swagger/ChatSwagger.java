package com.very.relink.chat.presentation.swagger;

import com.very.relink.chat.application.response.ChatResponses.ChatMessagesResponse;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomParticipantsResponse;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomsResponse;
import com.very.relink.chat.application.response.ChatResponses.CreateChatRoomResponse;
import com.very.relink.chat.application.response.ChatResponses.IssueChatAttachmentPresignedUrlResponse;
import com.very.relink.chat.application.response.ChatResponses.MarkChatRoomAsReadResponse;
import com.very.relink.chat.application.response.ChatResponses.SendChatMessageResponse;
import com.very.relink.chat.exception.ChatErrorCode;
import com.very.relink.chat.presentation.rest.ChatController.CreateDirectChatRoomRequest;
import com.very.relink.chat.presentation.rest.ChatController.CreateGroupChatRoomRequest;
import com.very.relink.chat.presentation.rest.ChatController.IssueChatAttachmentPresignedUrlRequest;
import com.very.relink.chat.presentation.rest.ChatController.MarkChatRoomAsReadRequest;
import com.very.relink.chat.presentation.rest.ChatController.SendChatMessageRequest;
import com.very.relink.core.configuration.swagger.ApiErrorCode;
import com.very.relink.core.presentation.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Chat", description = "채팅방, 메시지, 첨부 파일 API")
public interface ChatSwagger {

    @Operation(
            summary = "1:1 채팅방 생성",
            description = "현재 로그인한 회원과 대상 회원 사이의 1:1 채팅방을 생성합니다. 이미 존재하는 1:1 채팅방이 있으면 기존 채팅방 정보를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "1:1 채팅방 생성 성공",
            content = @Content(schema = @Schema(implementation = CreateChatRoomResponse.class))
    )
    @ApiErrorCode({ChatErrorCode.class})
    ResponseEntity<RestResponse<CreateChatRoomResponse>> createDirectRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "1:1 채팅을 시작할 대상 회원 정보",
                    required = true
            )
            @Valid @RequestBody CreateDirectChatRoomRequest request
    );

    @Operation(
            summary = "그룹 채팅방 생성",
            description = "현재 로그인한 회원을 포함한 그룹 채팅방을 생성합니다. 제목, 참여자 회원 ID 목록, 선택적인 커버 이미지 키를 전달합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "그룹 채팅방 생성 성공",
            content = @Content(schema = @Schema(implementation = CreateChatRoomResponse.class))
    )
    @ApiErrorCode({ChatErrorCode.class})
    ResponseEntity<RestResponse<CreateChatRoomResponse>> createGroupRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "그룹 채팅방 생성 정보",
                    required = true
            )
            @Valid @RequestBody CreateGroupChatRoomRequest request
    );

    @Operation(
            summary = "내 채팅방 목록 조회",
            description = "현재 로그인한 회원이 참여 중인 채팅방 목록을 최근 메시지 기준으로 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "채팅방 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ChatRoomsResponse.class))
    )
    @ApiErrorCode({ChatErrorCode.class})
    ResponseEntity<RestResponse<ChatRoomsResponse>> getRooms();

    @Operation(
            summary = "채팅방 참여자 목록 조회",
            description = "현재 로그인한 회원이 참여 중인 채팅방의 활성 참여자 목록을 조회합니다. 그룹 채팅방에서 약속을 제안할 때 방 인원을 약속 참여자로 채우는 데 사용합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "채팅방 참여자 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ChatRoomParticipantsResponse.class))
    )
    @ApiErrorCode({ChatErrorCode.class})
    ResponseEntity<RestResponse<ChatRoomParticipantsResponse>> getRoomParticipants(
            @Parameter(description = "참여자를 조회할 채팅방 ID", example = "1")
            @PathVariable Long roomId
    );

    @Operation(
            summary = "채팅 메시지 목록 조회",
            description = "채팅방의 메시지를 커서 기반으로 조회합니다. cursor를 생략하면 최신 메시지부터 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "채팅 메시지 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ChatMessagesResponse.class))
    )
    @ApiErrorCode({ChatErrorCode.class})
    ResponseEntity<RestResponse<ChatMessagesResponse>> getMessages(
            @Parameter(description = "메시지를 조회할 채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @Parameter(description = "이전 페이지의 nextCursor 값. 생략하면 최신 메시지부터 조회합니다.", example = "100")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "한 번에 조회할 메시지 개수", example = "30")
            @RequestParam(defaultValue = "30") int size
    );

    @Operation(
            summary = "채팅 메시지 전송",
            description = "채팅방에 텍스트 또는 이미지 메시지를 전송합니다. clientMessageId는 중복 전송 방지를 위해 클라이언트가 생성한 고유 값이어야 합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "채팅 메시지 전송 성공",
            content = @Content(schema = @Schema(implementation = SendChatMessageResponse.class))
    )
    @ApiErrorCode({ChatErrorCode.class})
    ResponseEntity<RestResponse<SendChatMessageResponse>> sendMessage(
            @Parameter(description = "메시지를 전송할 채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "전송할 메시지 내용과 첨부 파일 정보",
                    required = true
            )
            @Valid @RequestBody SendChatMessageRequest request
    );

    @Operation(
            summary = "채팅방 읽음 처리",
            description = "현재 로그인한 회원의 채팅방 읽음 커서를 지정한 메시지 ID까지 갱신합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "채팅방 읽음 처리 성공",
            content = @Content(schema = @Schema(implementation = MarkChatRoomAsReadResponse.class))
    )
    @ApiErrorCode({ChatErrorCode.class})
    ResponseEntity<RestResponse<MarkChatRoomAsReadResponse>> markAsRead(
            @Parameter(description = "읽음 처리할 채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "마지막으로 읽은 메시지 ID",
                    required = true
            )
            @Valid @RequestBody MarkChatRoomAsReadRequest request
    );

    @Operation(
            summary = "채팅 첨부 파일 업로드 URL 발급",
            description = "채팅 이미지 첨부 파일을 업로드할 수 있는 presigned URL을 발급합니다. 발급된 storageKey는 메시지 전송 시 첨부 파일 정보에 포함합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "첨부 파일 업로드 URL 발급 성공",
            content = @Content(schema = @Schema(implementation = IssueChatAttachmentPresignedUrlResponse.class))
    )
    @ApiErrorCode({ChatErrorCode.class})
    ResponseEntity<RestResponse<IssueChatAttachmentPresignedUrlResponse>> issuePresignedUrl(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업로드할 첨부 파일 메타데이터",
                    required = true
            )
            @Valid @RequestBody IssueChatAttachmentPresignedUrlRequest request
    );
}
