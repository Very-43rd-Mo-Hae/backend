package com.very.relink.auth.domain.token;

public record AuthTokens(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
}
