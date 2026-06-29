package com.very.relink.notification.presentation.swagger;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.notification.adapter.in.web.TestWebPushNotificationRequest;
import com.very.relink.notification.application.dto.WebPushSendResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Notification", description = "알림 발송 API")
public interface NotificationTestSwagger {

    @Operation(
            summary = "테스트 웹 푸시 알림 발송",
            description = "현재 로그인한 회원에게 테스트 웹 푸시 알림을 발송합니다. 푸시 구독 및 알림 payload 확인 용도로 사용합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "테스트 웹 푸시 알림 발송 성공",
            content = @Content(schema = @Schema(implementation = WebPushSendResult.class))
    )
    ResponseEntity<RestResponse<WebPushSendResult>> sendTest(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "테스트 알림 제목, 본문, 이동 링크, 추가 데이터",
                    required = true
            )
            @Valid @RequestBody TestWebPushNotificationRequest request
    );
}
