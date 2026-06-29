package com.very.relink.notification.presentation.swagger;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.notification.adapter.in.web.WebPushPublicKeyResponse;
import com.very.relink.notification.adapter.in.web.WebPushSubscriptionDeleteRequest;
import com.very.relink.notification.adapter.in.web.WebPushSubscriptionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Web Push Subscription", description = "웹 푸시 구독 API")
public interface WebPushSubscriptionSwagger {

    @Operation(
            summary = "웹 푸시 구독 등록",
            description = "현재 로그인한 회원의 브라우저 푸시 구독 정보를 등록하거나 갱신합니다. 브라우저 PushSubscription에서 받은 endpoint와 key 값을 전달합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "웹 푸시 구독 등록 성공",
            content = @Content(schema = @Schema(implementation = RestResponse.class))
    )
    ResponseEntity<RestResponse<Void>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "브라우저 웹 푸시 구독 정보",
                    required = true
            )
            @Valid @RequestBody WebPushSubscriptionRequest request,
            @Parameter(description = "구독을 등록한 클라이언트 User-Agent", example = "Mozilla/5.0")
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    );

    @Operation(
            summary = "웹 푸시 구독 해제",
            description = "현재 로그인한 회원의 특정 웹 푸시 구독을 비활성화합니다. 더 이상 사용하지 않는 endpoint를 전달합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "웹 푸시 구독 해제 성공",
            content = @Content(schema = @Schema(implementation = RestResponse.class))
    )
    ResponseEntity<RestResponse<Void>> disable(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "비활성화할 웹 푸시 구독 endpoint",
                    required = true
            )
            @Valid @RequestBody WebPushSubscriptionDeleteRequest request
    );

    @Operation(
            summary = "웹 푸시 공개키 조회",
            description = "브라우저에서 PushManager.subscribe 호출 시 applicationServerKey로 사용할 VAPID 공개키를 조회합니다."
    )
    @SecurityRequirements
    @ApiResponse(
            responseCode = "200",
            description = "웹 푸시 공개키 조회 성공",
            content = @Content(schema = @Schema(implementation = WebPushPublicKeyResponse.class))
    )
    ResponseEntity<RestResponse<WebPushPublicKeyResponse>> getPublicKey();
}
