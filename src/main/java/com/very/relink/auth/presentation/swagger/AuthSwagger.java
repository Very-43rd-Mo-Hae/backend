package com.very.relink.auth.presentation.swagger;

import com.very.relink.auth.adapter.in.security.CustomUserDetail;
import com.very.relink.auth.adapter.in.token.ReIssueTokenRequest;
import com.very.relink.auth.adapter.in.token.LogoutRequest;
import com.very.relink.auth.adapter.in.web.SocialLoginRequest;
import com.very.relink.auth.adapter.in.web.SocialLoginResponse;
import com.very.relink.auth.application.result.ReissueTokenResponse;
import com.very.relink.auth.exception.AuthErrorCode;
import com.very.relink.auth.exception.TokenErrorCode;
import com.very.relink.core.configuration.swagger.ApiErrorCode;
import com.very.relink.core.presentation.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Auth", description = "인증 API")
public interface AuthSwagger {

    @Operation(
            summary = "로그인",
            description = "클라이언트에서 받은 provider token으로 로그인하고 서비스 JWT를 발급합니다."
    )
    @SecurityRequirements
    @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = SocialLoginResponse.class))
    )
    @ApiErrorCode({AuthErrorCode.class})
    ResponseEntity<RestResponse<SocialLoginResponse>> login(
            @Valid @RequestBody SocialLoginRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    );

    @Operation(
            summary = "토큰 재발급",
            description = "refresh token을 검증하고 새로운 access token을 발급합니다."
    )
    @SecurityRequirements
    @ApiResponse(
            responseCode = "200",
            description = "토큰 재발급 성공",
            content = @Content(schema = @Schema(implementation = ReissueTokenResponse.class))
    )
    @ApiErrorCode({AuthErrorCode.class, TokenErrorCode.class})
    ResponseEntity<RestResponse<ReissueTokenResponse>> reissue(
            @RequestBody ReIssueTokenRequest reIssueTokenRequest
    );

    @Operation(
            summary = "로그아웃",
            description = "현재 로그인 세션을 만료 처리합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(schema = @Schema(implementation = RestResponse.class))
    )
    @ApiErrorCode({AuthErrorCode.class, TokenErrorCode.class})
    ResponseEntity<RestResponse<Void>> logout(
            @RequestBody LogoutRequest logoutRequest
    );

    @Operation(
            summary = "전체 로그아웃",
            description = "현재 회원의 모든 로그인 세션을 만료 처리합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "전체 로그아웃 성공",
            content = @Content(schema = @Schema(implementation = RestResponse.class))
    )
    @ApiErrorCode({AuthErrorCode.class, TokenErrorCode.class})
    ResponseEntity<RestResponse<Void>> logoutAll(@AuthenticationPrincipal CustomUserDetail customUserDetail);
}
