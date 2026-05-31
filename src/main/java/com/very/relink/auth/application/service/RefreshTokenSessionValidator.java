package com.very.relink.auth.application.service;

import com.very.relink.auth.application.port.out.GetRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.LoadAuthSessionPort;
import com.very.relink.auth.application.port.out.RefreshTokenHashPort;
import com.very.relink.auth.application.port.out.RefreshTokenIssuePort;
import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.auth.domain.token.RefreshTokenClaims;
import com.very.relink.auth.exception.TokenErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenSessionValidator {

    private final RefreshTokenIssuePort refreshTokenIssuePort;
    private final GetRefreshTokenCachePort getRefreshTokenCachePort;
    private final RefreshTokenHashPort refreshTokenHashPort;
    private final LoadAuthSessionPort loadAuthSessionPort;

    public VerifiedRefreshTokenSession verify(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.toException();
        }

        RefreshTokenClaims refreshTokenClaims = refreshTokenIssuePort.authenticateRefreshToken(refreshToken);
        String sessionId = refreshTokenClaims.sessionId();
        String tokenBySessionId = getRefreshTokenCachePort.getTokenBySessionId(sessionId);

        if (tokenBySessionId == null || tokenBySessionId.isBlank()) {
            throw TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.toException();
        }

        if (!refreshTokenHashPort.matches(refreshToken, tokenBySessionId)) {
            throw TokenErrorCode.REFRESH_TOKEN_MISMATCH.toException();
        }

        AuthSession authSession = loadAuthSessionPort.findBySessionId(sessionId)
                .orElseThrow(TokenErrorCode.AUTH_SESSION_NOT_FOUND::toException);

        validateClaims(authSession, refreshTokenClaims);

        return new VerifiedRefreshTokenSession(refreshToken, refreshTokenClaims, authSession);
    }

    private void validateClaims(AuthSession authSession, RefreshTokenClaims refreshTokenClaims) {
        if (!authSession.getRefreshTokenJti().equals(refreshTokenClaims.refreshTokenJti())) {
            throw TokenErrorCode.REFRESH_TOKEN_MISMATCH.toException();
        }

        if (!authSession.getMemberId().equals(refreshTokenClaims.memberId())) {
            throw TokenErrorCode.AUTH_SESSION_NOT_FOUND.toException();
        }

        if (!authSession.getSessionId().equals(refreshTokenClaims.sessionId())) {
            throw TokenErrorCode.AUTH_SESSION_NOT_FOUND.toException();
        }
    }
}
