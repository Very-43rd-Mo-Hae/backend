package com.very.relink.auth.domain.token;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long accessTokenExpiresIn,
        Long refreshTokenExpiresIn
) {
}
