package com.very.relink.auth.application.service;

import com.very.relink.auth.adapter.in.token.ReIssueTokenRequest;
import com.very.relink.auth.application.port.out.GetRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.LoadAuthSessionPort;
import com.very.relink.auth.application.port.out.RefreshTokenHashPort;
import com.very.relink.auth.application.port.out.RefreshTokenIssuePort;
import com.very.relink.auth.application.port.out.SaveAuthSessionPort;
import com.very.relink.auth.application.port.out.SaveRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.TokenIssuePort;
import com.very.relink.auth.application.result.ReissueTokenResponse;
import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.domain.token.RefreshTokenClaims;
import com.very.relink.auth.exception.TokenErrorCode;
import com.very.relink.member.application.port.out.LoadMemberPort;
import com.very.relink.member.domain.Member;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenIssuePort refreshTokenIssuePort;
    private final GetRefreshTokenCachePort getRefreshTokenCachePort;
    private final SaveRefreshTokenCachePort saveRefreshTokenCachePort;
    private final RefreshTokenHashPort refreshTokenHashPort;
    private final LoadAuthSessionPort loadAuthSessionPort;
    private final SaveAuthSessionPort saveAuthSessionPort;
    private final TokenIssuePort tokenIssuePort;
    private final LoadMemberPort loadMemberPort;

    @Transactional
    public ReissueTokenResponse reIssueToken(
            ReIssueTokenRequest reIssueTokenRequest
    ) {
        if (reIssueTokenRequest == null) {
            throw TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.toException();
        }

        String refreshToken = reIssueTokenRequest.refreshToken();

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

        if (!authSession.getRefreshTokenJti().equals(refreshTokenClaims.refreshTokenJti())) {
            throw TokenErrorCode.REFRESH_TOKEN_MISMATCH.toException();
        }

        if (!authSession.getMemberId().equals(refreshTokenClaims.memberId())) {
            throw TokenErrorCode.AUTH_SESSION_NOT_FOUND.toException();
        }

        if (!authSession.getSessionId().equals(sessionId)) {
            throw TokenErrorCode.AUTH_SESSION_NOT_FOUND.toException();
        }

        if (!authSession.isActive(LocalDateTime.now())) {
            throw TokenErrorCode.AUTH_SESSION_EXPIRED.toException();
        }

        Member member = loadMemberPort.findById(authSession.getMemberId())
                .orElseThrow(TokenErrorCode.AUTH_SESSION_NOT_FOUND::toException);

        String newRefreshTokenJti = UUID.randomUUID().toString();
        AuthTokens authTokens = tokenIssuePort.issue(member, sessionId, newRefreshTokenJti);
        String newRefreshTokenHash = refreshTokenHashPort.hash(authTokens.refreshToken());
        Duration refreshTokenTtl = Duration.ofSeconds(authTokens.refreshTokenExpiresIn());

        authSession.rotateRefreshToken(newRefreshTokenJti, newRefreshTokenHash, LocalDateTime.now());
        saveAuthSessionPort.save(authSession);
        saveRefreshTokenCachePort.save(sessionId, newRefreshTokenHash, refreshTokenTtl);

        return new ReissueTokenResponse(
                authTokens.accessToken(),
                authTokens.refreshToken(),
                authTokens.accessTokenExpiresIn(),
                authTokens.refreshTokenExpiresIn()
        );
    }
}
