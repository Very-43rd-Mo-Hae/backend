package com.very.relink.auth.application.service;

import com.very.relink.auth.adapter.in.token.LogoutRequest;
import com.very.relink.auth.adapter.in.token.ReIssueTokenRequest;
import com.very.relink.auth.application.port.out.DeleteRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.RefreshTokenHashPort;
import com.very.relink.auth.application.port.out.SaveAuthSessionPort;
import com.very.relink.auth.application.port.out.SaveRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.TokenIssuePort;
import com.very.relink.auth.application.result.ReissueTokenResponse;
import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.auth.domain.session.AuthSessionStatus;
import com.very.relink.auth.domain.token.AuthTokens;
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

    private final SaveRefreshTokenCachePort saveRefreshTokenCachePort;
    private final DeleteRefreshTokenCachePort deleteRefreshTokenCachePort;
    private final RefreshTokenHashPort refreshTokenHashPort;
    private final RefreshTokenSessionValidator refreshTokenSessionValidator;
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

        VerifiedRefreshTokenSession verifiedSession = refreshTokenSessionValidator.verify(
                reIssueTokenRequest.refreshToken()
        );
        AuthSession authSession = verifiedSession.authSession();
        validateActiveSessionForReissue(authSession, LocalDateTime.now());

        Member member = loadMemberPort.findById(authSession.getMemberId())
                .orElseThrow(TokenErrorCode.AUTH_SESSION_NOT_FOUND::toException);

        String newRefreshTokenJti = UUID.randomUUID().toString();
        AuthTokens authTokens = tokenIssuePort.issue(member, verifiedSession.sessionId(), newRefreshTokenJti);
        String newRefreshTokenHash = refreshTokenHashPort.hash(authTokens.refreshToken());
        Duration refreshTokenTtl = Duration.ofSeconds(authTokens.refreshTokenExpiresIn());

        authSession.rotateRefreshToken(newRefreshTokenJti, newRefreshTokenHash, LocalDateTime.now());
        saveAuthSessionPort.save(authSession);
        saveRefreshTokenCachePort.save(verifiedSession.sessionId(), newRefreshTokenHash, refreshTokenTtl);

        return new ReissueTokenResponse(
                authTokens.accessToken(),
                authTokens.refreshToken(),
                authTokens.accessTokenExpiresIn(),
                authTokens.refreshTokenExpiresIn()
        );
    }

    @Transactional
    public void logout(LogoutRequest logoutRequest) {
        if (logoutRequest == null) {
            throw TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.toException();
        }

        VerifiedRefreshTokenSession verifiedSession = refreshTokenSessionValidator.verify(logoutRequest.refreshToken());
        AuthSession authSession = verifiedSession.authSession();
        validateActiveSessionForLogout(authSession, LocalDateTime.now());

        authSession.logout(LocalDateTime.now());
        saveAuthSessionPort.save(authSession);
        deleteRefreshTokenCachePort.deleteBySessionId(verifiedSession.sessionId());
    }

    private void validateActiveSessionForReissue(AuthSession authSession, LocalDateTime now) {
        if (!authSession.isActive(now)) {
            throw TokenErrorCode.AUTH_SESSION_EXPIRED.toException();
        }
    }

    private void validateActiveSessionForLogout(AuthSession authSession, LocalDateTime now) {
        if (authSession.isActive(now)) {
            return;
        }

        if (authSession.getStatus() == AuthSessionStatus.LOGGED_OUT) {
            throw TokenErrorCode.AUTH_SESSION_LOGGED_OUT.toException();
        }

        if (authSession.getStatus() == AuthSessionStatus.REVOKED) {
            throw TokenErrorCode.AUTH_SESSION_REVOKED.toException();
        }

        throw TokenErrorCode.AUTH_SESSION_EXPIRED.toException();
    }
}
