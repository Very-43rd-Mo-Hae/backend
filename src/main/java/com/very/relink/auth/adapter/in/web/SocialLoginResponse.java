package com.very.relink.auth.adapter.in.web;

import com.very.relink.auth.domain.token.AuthTokens;

public record SocialLoginResponse(
        Long memberId,
        String accessToken,
        String refreshToken,
        Long accessTokenExpiresIn,
        boolean requiresAccountRestore
) {

    public static SocialLoginResponse from(Long memberId, AuthTokens authTokens, boolean requiresAccountRestore) {
        return new SocialLoginResponse(
                memberId,
                authTokens == null ? null : authTokens.accessToken(),
                authTokens == null ? null : authTokens.refreshToken(),
                authTokens == null ? null : authTokens.accessTokenExpiresIn(),
                requiresAccountRestore
        );
    }
}
