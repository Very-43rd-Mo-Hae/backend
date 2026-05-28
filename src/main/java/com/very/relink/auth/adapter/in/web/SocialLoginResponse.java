package com.very.relink.auth.adapter.in.web;

import com.very.relink.auth.domain.token.AuthTokens;

public record SocialLoginResponse(
        Long memberId,
        String accessToken,
        String refreshToken,
        Long accessTokenExpiresIn
) {

    public static SocialLoginResponse from(Long memberId, AuthTokens authTokens) {
        return new SocialLoginResponse(
                memberId,
                authTokens.accessToken(),
                authTokens.refreshToken(),
                authTokens.accessTokenExpiresIn()
        );
    }
}
