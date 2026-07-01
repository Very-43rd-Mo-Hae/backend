package com.very.relink.notification.presentation.swagger;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.notification.application.response.NotificationInboxResponses.NotificationInboxResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Notification Inbox", description = "알림함 조회와 읽음/삭제 처리 API")
public interface NotificationInboxSwagger {

    @Operation(
            summary = "알림함 목록 조회",
            description = "현재 로그인한 회원의 알림함 목록을 페이지 단위로 조회합니다. 읽지 않은 알림 존재 여부도 함께 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "알림함 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = NotificationInboxResponse.class))
    )
    ResponseEntity<RestResponse<NotificationInboxResponse>> getNotifications(
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "모든 알림 읽음 처리",
            description = "현재 로그인한 회원의 알림함에 있는 모든 알림을 읽음 상태로 변경합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "모든 알림 읽음 처리 성공",
            content = @Content(schema = @Schema(implementation = RestResponse.class))
    )
    ResponseEntity<RestResponse<Void>> markAllRead();

    @Operation(
            summary = "알림 삭제",
            description = "현재 로그인한 회원의 알림함에서 지정한 알림 항목을 삭제합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "알림 삭제 성공",
            content = @Content(schema = @Schema(implementation = RestResponse.class))
    )
    ResponseEntity<RestResponse<Void>> deleteNotification(
            @Parameter(description = "삭제할 알림함 항목 ID", example = "1")
            @PathVariable Long notificationInboxItemId
    );
}
