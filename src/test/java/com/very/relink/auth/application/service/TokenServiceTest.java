package com.very.relink.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.very.relink.auth.adapter.in.token.LogoutRequest;
import com.very.relink.auth.adapter.in.token.ReIssueTokenRequest;
import com.very.relink.auth.application.port.out.DeleteRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.GetRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.LoadAuthSessionPort;
import com.very.relink.auth.application.port.out.RefreshTokenHashPort;
import com.very.relink.auth.application.port.out.RefreshTokenIssuePort;
import com.very.relink.auth.application.port.out.SaveAuthSessionPort;
import com.very.relink.auth.application.port.out.SaveRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.TokenIssuePort;
import com.very.relink.auth.application.result.ReissueTokenResponse;
import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.auth.domain.session.AuthSessionStatus;
import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.domain.token.RefreshTokenClaims;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.auth.exception.TokenErrorCode;
import com.very.relink.core.exception.DomainException;
import com.very.relink.member.application.port.out.LoadMemberPort;
import com.very.relink.member.domain.Member;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenServiceTest {

    private static final String CURRENT_REFRESH_TOKEN = "current-refresh-token";
    private static final String SESSION_ID = "session-id";
    private static final String OLD_REFRESH_TOKEN_JTI = "old-jti";
    private static final String CURRENT_REFRESH_TOKEN_HASH = "hashed-current-refresh-token";

    @Test
    @DisplayName("RefreshToken을 재발급 하고 AuthSession을 갱신한다.")
    void reIssueTokenRotatesRefreshToken() {
        AuthSession authSession = activeAuthSession();
        Member member = member();
        FakeRefreshTokenIssuePort refreshTokenIssuePort = new FakeRefreshTokenIssuePort();
        FakeSaveRefreshTokenCachePort saveRefreshTokenCachePort = new FakeSaveRefreshTokenCachePort();
        FakeDeleteRefreshTokenCachePort deleteRefreshTokenCachePort = new FakeDeleteRefreshTokenCachePort();
        FakeSaveAuthSessionPort saveAuthSessionPort = new FakeSaveAuthSessionPort();
        FakeTokenIssuePort tokenIssuePort = new FakeTokenIssuePort();
        FakeRefreshTokenHashPort refreshTokenHashPort = new FakeRefreshTokenHashPort();
        FakeLoadAuthSessionPort loadAuthSessionPort = new FakeLoadAuthSessionPort(
                sessionId -> Optional.of(authSession)
        );
        RefreshTokenSessionValidator refreshTokenSessionValidator = new RefreshTokenSessionValidator(
                refreshTokenIssuePort,
                sessionId -> CURRENT_REFRESH_TOKEN_HASH,
                refreshTokenHashPort,
                loadAuthSessionPort
        );
        TokenService tokenService = new TokenService(
                saveRefreshTokenCachePort,
                deleteRefreshTokenCachePort,
                refreshTokenHashPort,
                refreshTokenSessionValidator,
                saveAuthSessionPort,
                loadAuthSessionPort,
                tokenIssuePort,
                new FakeLoadMemberPort(member)
        );

        ReissueTokenResponse response = tokenService.reIssueToken(
                new ReIssueTokenRequest(CURRENT_REFRESH_TOKEN)
        );

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.accessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(response.refreshTokenExpiresIn()).isEqualTo(1209600L);
        assertThat(tokenIssuePort.issuedMember).isEqualTo(member);
        assertThat(tokenIssuePort.issuedSessionId).isEqualTo(SESSION_ID);
        assertThat(tokenIssuePort.issuedRefreshTokenJti).isNotBlank();
        assertThat(tokenIssuePort.issuedRefreshTokenJti).isNotEqualTo(OLD_REFRESH_TOKEN_JTI);
        assertThat(saveAuthSessionPort.savedAuthSession.getRefreshTokenJti())
                .isEqualTo(tokenIssuePort.issuedRefreshTokenJti);
        assertThat(saveAuthSessionPort.savedAuthSession.getRefreshTokenHash())
                .isEqualTo("hashed-new-refresh-token");
        assertThat(saveRefreshTokenCachePort.savedSessionId).isEqualTo(SESSION_ID);
        assertThat(saveRefreshTokenCachePort.savedRefreshTokenHash).isEqualTo("hashed-new-refresh-token");
        assertThat(saveRefreshTokenCachePort.savedTtl).isEqualTo(Duration.ofSeconds(1209600L));
    }

    @Test
    @DisplayName("현재 로그인 세션을 로그아웃 하고, 세션 정보를 제거한다")
    void logoutCurrentSession() {
        AuthSession authSession = activeAuthSession();
        FakeSaveAuthSessionPort saveAuthSessionPort = new FakeSaveAuthSessionPort();
        FakeDeleteRefreshTokenCachePort deleteRefreshTokenCachePort = new FakeDeleteRefreshTokenCachePort();
        TokenService tokenService = tokenService(authSession, member(), deleteRefreshTokenCachePort, saveAuthSessionPort);

        tokenService.logout(new LogoutRequest(CURRENT_REFRESH_TOKEN));

        assertThat(saveAuthSessionPort.savedAuthSession.getStatus()).isEqualTo(AuthSessionStatus.LOGGED_OUT);
        assertThat(saveAuthSessionPort.savedAuthSession.getLoggedOutAt()).isNotNull();
        assertThat(deleteRefreshTokenCachePort.deletedSessionId).isEqualTo(SESSION_ID);
    }

    @Test
    @DisplayName("재발급 요청이 null이면 실패한다")
    void reIssueTokenFailsWhenRequestIsNull() {
        TokenService tokenService = tokenService(activeAuthSession(), member());

        assertThatThrownBy(() -> tokenService.reIssueToken(null))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("재발급 refreshToken이 공백이면 실패한다")
    void reIssueTokenFailsWhenRefreshTokenIsBlank() {
        TokenService tokenService = tokenService(activeAuthSession(), member());

        assertThatThrownBy(() -> tokenService.reIssueToken(new ReIssueTokenRequest(" ")))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("재발급 시 Redis refreshToken hash가 없으면 실패한다")
    void reIssueTokenFailsWhenRefreshTokenCacheIsMissing() {
        TokenService tokenService = tokenService(
                new FakeRefreshTokenIssuePort(),
                sessionId -> null,
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.of(activeAuthSession()),
                member()
        );

        assertThatThrownBy(() -> tokenService.reIssueToken(new ReIssueTokenRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("재발급 시 refreshToken hash가 일치하지 않으면 실패한다")
    void reIssueTokenFailsWhenRefreshTokenHashMismatches() {
        TokenService tokenService = tokenService(
                new FakeRefreshTokenIssuePort(),
                sessionId -> "different-refresh-token-hash",
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.of(activeAuthSession()),
                member()
        );

        assertThatThrownBy(() -> tokenService.reIssueToken(new ReIssueTokenRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.REFRESH_TOKEN_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("재발급 시 DB 세션이 없으면 실패한다")
    void reIssueTokenFailsWhenAuthSessionIsMissing() {
        TokenService tokenService = tokenService(
                new FakeRefreshTokenIssuePort(),
                sessionId -> CURRENT_REFRESH_TOKEN_HASH,
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.empty(),
                member()
        );

        assertThatThrownBy(() -> tokenService.reIssueToken(new ReIssueTokenRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.AUTH_SESSION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("재발급 시 memberId가 일치하지 않으면 실패한다")
    void reIssueTokenFailsWhenMemberIdMismatches() {
        TokenService tokenService = tokenService(
                new FakeRefreshTokenIssuePort(new RefreshTokenClaims(2L, SESSION_ID, OLD_REFRESH_TOKEN_JTI)),
                sessionId -> CURRENT_REFRESH_TOKEN_HASH,
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.of(activeAuthSession()),
                member()
        );

        assertThatThrownBy(() -> tokenService.reIssueToken(new ReIssueTokenRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.AUTH_SESSION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("재발급 시 refreshTokenJti가 일치하지 않으면 실패한다")
    void reIssueTokenFailsWhenRefreshTokenJtiMismatches() {
        TokenService tokenService = tokenService(
                new FakeRefreshTokenIssuePort(new RefreshTokenClaims(1L, SESSION_ID, "different-jti")),
                sessionId -> CURRENT_REFRESH_TOKEN_HASH,
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.of(activeAuthSession()),
                member()
        );

        assertThatThrownBy(() -> tokenService.reIssueToken(new ReIssueTokenRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.REFRESH_TOKEN_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("재발급 시 세션이 ACTIVE가 아니면 실패한다")
    void reIssueTokenFailsWhenAuthSessionIsNotActive() {
        TokenService tokenService = tokenService(loggedOutAuthSession(), member());

        assertThatThrownBy(() -> tokenService.reIssueToken(new ReIssueTokenRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.AUTH_SESSION_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("재발급 시 세션이 만료되었으면 실패한다")
    void reIssueTokenFailsWhenAuthSessionIsExpired() {
        TokenService tokenService = tokenService(expiredAuthSession(), member());

        assertThatThrownBy(() -> tokenService.reIssueToken(new ReIssueTokenRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.AUTH_SESSION_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("재발급 시 회원이 없으면 실패한다")
    void reIssueTokenFailsWhenMemberIsMissing() {
        TokenService tokenService = tokenService(activeAuthSession(), null);

        assertThatThrownBy(() -> tokenService.reIssueToken(new ReIssueTokenRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.AUTH_SESSION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("로그아웃 요청이 null이면 실패한다")
    void logoutFailsWhenRequestIsNull() {
        TokenService tokenService = tokenService(activeAuthSession(), member());

        assertThatThrownBy(() -> tokenService.logout(null))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("로그아웃 refreshToken이 공백이면 실패한다")
    void logoutFailsWhenRefreshTokenIsBlank() {
        TokenService tokenService = tokenService(activeAuthSession(), member());

        assertThatThrownBy(() -> tokenService.logout(new LogoutRequest(" ")))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("로그아웃 시 Redis refreshToken hash가 없으면 실패한다")
    void logoutFailsWhenRefreshTokenCacheIsMissing() {
        TokenService tokenService = tokenService(
                new FakeRefreshTokenIssuePort(),
                sessionId -> null,
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.of(activeAuthSession()),
                member()
        );

        assertThatThrownBy(() -> tokenService.logout(new LogoutRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("로그아웃 시 refreshToken hash가 일치하지 않으면 실패한다")
    void logoutFailsWhenRefreshTokenHashMismatches() {
        TokenService tokenService = tokenService(
                new FakeRefreshTokenIssuePort(),
                sessionId -> "different-refresh-token-hash",
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.of(activeAuthSession()),
                member()
        );

        assertThatThrownBy(() -> tokenService.logout(new LogoutRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.REFRESH_TOKEN_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("로그아웃 시 DB 세션이 없으면 실패한다")
    void logoutFailsWhenAuthSessionIsMissing() {
        TokenService tokenService = tokenService(
                new FakeRefreshTokenIssuePort(),
                sessionId -> CURRENT_REFRESH_TOKEN_HASH,
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.empty(),
                member()
        );

        assertThatThrownBy(() -> tokenService.logout(new LogoutRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.AUTH_SESSION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 로그아웃된 세션은 로그아웃에 실패한다")
    void logoutFailsWhenAuthSessionAlreadyLoggedOut() {
        TokenService tokenService = tokenService(loggedOutAuthSession(), member());

        assertThatThrownBy(() -> tokenService.logout(new LogoutRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.AUTH_SESSION_LOGGED_OUT.getMessage());
    }

    @Test
    @DisplayName("폐기된 세션은 로그아웃에 실패한다")
    void logoutFailsWhenAuthSessionIsRevoked() {
        TokenService tokenService = tokenService(revokedAuthSession(), member());

        assertThatThrownBy(() -> tokenService.logout(new LogoutRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.AUTH_SESSION_REVOKED.getMessage());
    }

    @Test
    @DisplayName("만료된 세션은 로그아웃에 실패한다")
    void logoutFailsWhenAuthSessionIsExpired() {
        TokenService tokenService = tokenService(expiredAuthSession(), member());

        assertThatThrownBy(() -> tokenService.logout(new LogoutRequest(CURRENT_REFRESH_TOKEN)))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.AUTH_SESSION_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("현재 회원의 모든 활성 세션을 로그아웃 처리하고 refreshToken 캐시를 삭제한다.")
    void logoutAllActiveSessions() {
        AuthSession firstSession = authSession(
                1L,
                "session-id-1",
                1L,
                AuthSessionStatus.ACTIVE,
                LocalDateTime.now().plusDays(14)
        );
        AuthSession secondSession = authSession(
                2L,
                "session-id-2",
                1L,
                AuthSessionStatus.ACTIVE,
                LocalDateTime.now().plusDays(14)
        );
        AuthSession otherMemberSession = authSession(
                3L,
                "session-id-3",
                2L,
                AuthSessionStatus.ACTIVE,
                LocalDateTime.now().plusDays(14)
        );
        AuthSession loggedOutSession = authSession(
                4L,
                "session-id-4",
                1L,
                AuthSessionStatus.LOGGED_OUT,
                LocalDateTime.now().plusDays(14)
        );
        FakeLoadAuthSessionPort loadAuthSessionPort = new FakeLoadAuthSessionPort(
                sessionId -> Optional.empty(),
                List.of(firstSession, secondSession, otherMemberSession, loggedOutSession)
        );
        FakeSaveAuthSessionPort saveAuthSessionPort = new FakeSaveAuthSessionPort();
        FakeDeleteRefreshTokenCachePort deleteRefreshTokenCachePort = new FakeDeleteRefreshTokenCachePort();
        TokenService tokenService = tokenService(
                new FakeRefreshTokenIssuePort(),
                sessionId -> CURRENT_REFRESH_TOKEN_HASH,
                new FakeRefreshTokenHashPort(),
                loadAuthSessionPort,
                member(),
                deleteRefreshTokenCachePort,
                saveAuthSessionPort
        );

        tokenService.logoutAll(1L);

        assertThat(saveAuthSessionPort.savedAuthSessions)
                .extracting(AuthSession::getSessionId)
                .containsExactly("session-id-1", "session-id-2");
        assertThat(saveAuthSessionPort.savedAuthSessions)
                .extracting(AuthSession::getStatus)
                .containsExactly(AuthSessionStatus.LOGGED_OUT, AuthSessionStatus.LOGGED_OUT);
        assertThat(deleteRefreshTokenCachePort.deletedSessionIds)
                .containsExactly("session-id-1", "session-id-2");
    }

    private static TokenService tokenService(AuthSession authSession, Member member) {
        return tokenService(authSession, member, new FakeDeleteRefreshTokenCachePort(), new FakeSaveAuthSessionPort());
    }

    private static TokenService tokenService(
            AuthSession authSession,
            Member member,
            FakeDeleteRefreshTokenCachePort deleteRefreshTokenCachePort,
            FakeSaveAuthSessionPort saveAuthSessionPort
    ) {
        return tokenService(
                new FakeRefreshTokenIssuePort(),
                sessionId -> CURRENT_REFRESH_TOKEN_HASH,
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.of(authSession),
                member,
                deleteRefreshTokenCachePort,
                saveAuthSessionPort
        );
    }

    private static TokenService tokenService(
            RefreshTokenIssuePort refreshTokenIssuePort,
            GetRefreshTokenCachePort getRefreshTokenCachePort,
            RefreshTokenHashPort refreshTokenHashPort,
            Function<String, Optional<AuthSession>> authSessionLoader,
            Member member
    ) {
        return tokenService(
                refreshTokenIssuePort,
                getRefreshTokenCachePort,
                refreshTokenHashPort,
                new FakeLoadAuthSessionPort(authSessionLoader),
                member,
                new FakeDeleteRefreshTokenCachePort(),
                new FakeSaveAuthSessionPort()
        );
    }

    private static TokenService tokenService(
            RefreshTokenIssuePort refreshTokenIssuePort,
            GetRefreshTokenCachePort getRefreshTokenCachePort,
            RefreshTokenHashPort refreshTokenHashPort,
            Function<String, Optional<AuthSession>> authSessionLoader,
            Member member,
            FakeDeleteRefreshTokenCachePort deleteRefreshTokenCachePort,
            FakeSaveAuthSessionPort saveAuthSessionPort
    ) {
        return tokenService(
                refreshTokenIssuePort,
                getRefreshTokenCachePort,
                refreshTokenHashPort,
                new FakeLoadAuthSessionPort(authSessionLoader),
                member,
                deleteRefreshTokenCachePort,
                saveAuthSessionPort
        );
    }

    private static TokenService tokenService(
            RefreshTokenIssuePort refreshTokenIssuePort,
            GetRefreshTokenCachePort getRefreshTokenCachePort,
            RefreshTokenHashPort refreshTokenHashPort,
            LoadAuthSessionPort loadAuthSessionPort,
            Member member,
            FakeDeleteRefreshTokenCachePort deleteRefreshTokenCachePort,
            FakeSaveAuthSessionPort saveAuthSessionPort
    ) {
        RefreshTokenSessionValidator refreshTokenSessionValidator = new RefreshTokenSessionValidator(
                refreshTokenIssuePort,
                getRefreshTokenCachePort,
                refreshTokenHashPort,
                loadAuthSessionPort
        );

        return new TokenService(
                new FakeSaveRefreshTokenCachePort(),
                deleteRefreshTokenCachePort,
                refreshTokenHashPort,
                refreshTokenSessionValidator,
                saveAuthSessionPort,
                loadAuthSessionPort,
                new FakeTokenIssuePort(),
                new FakeLoadMemberPort(member)
        );
    }

    private static AuthSession activeAuthSession() {
        return authSession(AuthSessionStatus.ACTIVE, LocalDateTime.now().plusDays(14));
    }

    private static AuthSession loggedOutAuthSession() {
        return authSession(AuthSessionStatus.LOGGED_OUT, LocalDateTime.now().plusDays(14));
    }

    private static AuthSession revokedAuthSession() {
        return authSession(AuthSessionStatus.REVOKED, LocalDateTime.now().plusDays(14));
    }

    private static AuthSession expiredAuthSession() {
        return authSession(AuthSessionStatus.ACTIVE, LocalDateTime.now().minusSeconds(1));
    }

    private static AuthSession authSession(AuthSessionStatus status, LocalDateTime expiresAt) {
        return authSession(1L, SESSION_ID, 1L, status, expiresAt);
    }

    private static AuthSession authSession(
            Long id,
            String sessionId,
            Long memberId,
            AuthSessionStatus status,
            LocalDateTime expiresAt
    ) {
        return AuthSession.builder()
                .id(id)
                .sessionId(sessionId)
                .memberId(memberId)
                .deviceId("device-id")
                .deviceName("device-name")
                .userAgent("user-agent")
                .refreshTokenJti(OLD_REFRESH_TOKEN_JTI)
                .refreshTokenHash(CURRENT_REFRESH_TOKEN_HASH)
                .status(status)
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiresAt(expiresAt)
                .build();
    }

    private static Member member() {
        return Member.builder()
                .id(1L)
                .email("member@example.com")
                .name("member")
                .imageUrl("https://example.com/profile.png")
                .provider(OAuth2Provider.KAKAO)
                .providerId("provider-id")
                .build();
    }

    private record FakeRefreshTokenIssuePort(RefreshTokenClaims refreshTokenClaims) implements RefreshTokenIssuePort {

            private FakeRefreshTokenIssuePort() {
                this(new RefreshTokenClaims(1L, SESSION_ID, OLD_REFRESH_TOKEN_JTI));
            }

        @Override
            public String issueRefreshToken(Long memberId, String sessionId, String refreshTokenJti) {
                return "new-refresh-token";
            }

            @Override
            public RefreshTokenClaims authenticateRefreshToken(String refreshToken) {
                return refreshTokenClaims;
            }
        }

    private static class FakeRefreshTokenHashPort implements RefreshTokenHashPort {

        @Override
        public String hash(String refreshToken) {
            return "hashed-" + refreshToken;
        }

        @Override
        public boolean matches(String refreshToken, String refreshTokenHash) {
            return hash(refreshToken).equals(refreshTokenHash);
        }
    }

    private static class FakeSaveRefreshTokenCachePort implements SaveRefreshTokenCachePort {

        private String savedSessionId;
        private String savedRefreshTokenHash;
        private Duration savedTtl;

        @Override
        public void save(String sessionId, String refreshTokenHash, Duration ttl) {
            this.savedSessionId = sessionId;
            this.savedRefreshTokenHash = refreshTokenHash;
            this.savedTtl = ttl;
        }
    }

    private static class FakeDeleteRefreshTokenCachePort implements DeleteRefreshTokenCachePort {

        private String deletedSessionId;
        private final List<String> deletedSessionIds = new ArrayList<>();

        @Override
        public void deleteBySessionId(String sessionId) {
            this.deletedSessionId = sessionId;
            this.deletedSessionIds.add(sessionId);
        }
    }

    private static class FakeSaveAuthSessionPort implements SaveAuthSessionPort {

        private AuthSession savedAuthSession;
        private final List<AuthSession> savedAuthSessions = new ArrayList<>();

        @Override
        public AuthSession save(AuthSession authSession) {
            this.savedAuthSession = authSession;
            this.savedAuthSessions.add(authSession);
            return authSession;
        }
    }

    private static class FakeLoadAuthSessionPort implements LoadAuthSessionPort {

        private final Function<String, Optional<AuthSession>> authSessionLoader;
        private final List<AuthSession> authSessions;

        private FakeLoadAuthSessionPort(Function<String, Optional<AuthSession>> authSessionLoader) {
            this(authSessionLoader, List.of());
        }

        private FakeLoadAuthSessionPort(
                Function<String, Optional<AuthSession>> authSessionLoader,
                List<AuthSession> authSessions
        ) {
            this.authSessionLoader = authSessionLoader;
            this.authSessions = authSessions;
        }

        @Override
        public Optional<AuthSession> findBySessionId(String sessionId) {
            return authSessionLoader.apply(sessionId);
        }

        @Override
        public List<AuthSession> findAllByMemberIdAndStatus(Long memberId, AuthSessionStatus status) {
            return authSessions.stream()
                    .filter(authSession -> authSession.getMemberId().equals(memberId))
                    .filter(authSession -> authSession.getStatus() == status)
                    .toList();
        }
    }

    private static class FakeTokenIssuePort implements TokenIssuePort {

        private Member issuedMember;
        private String issuedSessionId;
        private String issuedRefreshTokenJti;

        @Override
        public AuthTokens issue(Member member, String sessionId, String refreshTokenJti) {
            this.issuedMember = member;
            this.issuedSessionId = sessionId;
            this.issuedRefreshTokenJti = refreshTokenJti;
            return new AuthTokens(
                    "new-access-token",
                    "new-refresh-token",
                    "Bearer",
                    3600L,
                    1209600L
            );
        }
    }

    private record FakeLoadMemberPort(Member member) implements LoadMemberPort {

        @Override
        public Optional<Member> findById(Long id) {
            return Optional.ofNullable(member).filter(value -> value.getId().equals(id));
        }

        @Override
        public Optional<Member> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<Member> findByProviderAndProviderId(
                OAuth2Provider provider,
                String providerId
        ) {
            return Optional.empty();
        }
    }
}
