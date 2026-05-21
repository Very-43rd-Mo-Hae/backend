package com.very.relink.auth.presentation.controller;

import com.very.relink.auth.adapter.in.web.SocialLoginRequest;
import com.very.relink.auth.adapter.in.web.SocialLoginResponse;
import com.very.relink.auth.application.command.SocialLoginCommand;
import com.very.relink.auth.application.port.in.SocialLoginUseCase;
import com.very.relink.auth.application.result.OAuth2LoginResult;
import com.very.relink.auth.presentation.swagger.SocialLoginSwagger;
import com.very.relink.core.presentation.RestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/social")
public class SocialLoginController implements SocialLoginSwagger {

    private final SocialLoginUseCase socialLoginUseCase;

    @Override
    @PostMapping("/login")
    public ResponseEntity<RestResponse<SocialLoginResponse>> login(
            @Valid @RequestBody SocialLoginRequest socialLoginRequest
    ) {
        OAuth2LoginResult result = socialLoginUseCase.login(
                new SocialLoginCommand(
                        socialLoginRequest.provider(),
                        socialLoginRequest.idToken(),
                        socialLoginRequest.accessToken(),
                        socialLoginRequest.name()
                )
        );

        return ResponseEntity.ok(
                new RestResponse<>(
                        SocialLoginResponse.from(result.memberId(), result.authTokens())
                )
        );
    }
}
