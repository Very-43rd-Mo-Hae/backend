package com.very.relink.auth.application.service;

import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.auth.domain.token.RefreshTokenClaims;

public record VerifiedRefreshTokenSession(
        String refreshToken,
        RefreshTokenClaims claims,
        AuthSession authSession
) {

    public String sessionId() {
        return claims.sessionId();
    }
}
