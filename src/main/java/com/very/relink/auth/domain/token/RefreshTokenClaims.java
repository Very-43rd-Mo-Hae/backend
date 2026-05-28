package com.very.relink.auth.domain.token;

public record RefreshTokenClaims(
        Long memberId,
        String sessionId,
        String refreshTokenJti
) {
}
