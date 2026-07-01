package com.very.relink.auth.presentation.controller;

import com.very.relink.auth.adapter.in.security.CustomUserDetail;
import com.very.relink.auth.adapter.in.token.ReIssueTokenRequest;
import com.very.relink.auth.adapter.in.token.LogoutRequest;
import com.very.relink.auth.adapter.in.web.SocialLoginRequest;
import com.very.relink.auth.adapter.in.web.SocialLoginResponse;
import com.very.relink.auth.application.command.SocialLoginCommand;
import com.very.relink.auth.application.port.in.SocialLoginUseCase;
import com.very.relink.auth.application.result.OAuth2LoginResult;
import com.very.relink.auth.application.result.ReissueTokenResponse;
import com.very.relink.auth.application.service.TokenService;
import com.very.relink.auth.presentation.swagger.AuthSwagger;
import com.very.relink.core.presentation.RestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthSwagger {

    private final SocialLoginUseCase socialLoginUseCase;
    private final TokenService tokenService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<RestResponse<SocialLoginResponse>> login(
            @Valid @RequestBody SocialLoginRequest socialLoginRequest,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        OAuth2LoginResult result = socialLoginUseCase.login(
                new SocialLoginCommand(
                        socialLoginRequest.provider(),
                        socialLoginRequest.idToken(),
                        socialLoginRequest.accessToken(),
                        socialLoginRequest.name(),
                        socialLoginRequest.deviceId(),
                        socialLoginRequest.deviceName(),
                        userAgent,
                        socialLoginRequest.shouldRestoreAccount()
                )
        );

        return ResponseEntity.ok(
                new RestResponse<>(
                        SocialLoginResponse.from(result.memberId(), result.authTokens(), result.requiresAccountRestore())
                )
        );
    }

    @Override
    @PostMapping("/reissue")
    public ResponseEntity<RestResponse<ReissueTokenResponse>> reissue(
            @RequestBody ReIssueTokenRequest reIssueTokenRequest
    ) {
        return ResponseEntity.ok(
                new RestResponse<>(tokenService.reIssueToken(reIssueTokenRequest))
        );
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<RestResponse<Void>> logout(
            @RequestBody LogoutRequest logoutRequest
    ) {
        tokenService.logout(logoutRequest);
        return ResponseEntity.ok(new RestResponse<>(null));
    }

    @Override
    @PostMapping("/logout/all")
    public ResponseEntity<RestResponse<Void>> logoutAll(
            @AuthenticationPrincipal CustomUserDetail customUserDetail
    ) {
        tokenService.logoutAll(customUserDetail.getMemberId());
        return ResponseEntity.ok(new RestResponse<>(null));
    }
}
