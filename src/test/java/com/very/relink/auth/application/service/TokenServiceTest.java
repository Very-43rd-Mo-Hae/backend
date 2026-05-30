package com.very.relink.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.very.relink.auth.adapter.in.token.ReIssueTokenRequest;
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
import com.very.relink.member.application.port.out.LoadMemberPort;
import com.very.relink.member.domain.Member;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenServiceTest {

    @Test
    @DisplayName("Reissue rotates refresh token and updates session cache.")
    void reIssueTokenRotatesRefreshToken() {
        AuthSession authSession = AuthSession.builder()
                .id(1L)
                .sessionId("session-id")
                .memberId(1L)
                .deviceId("device-id")
                .deviceName("device-name")
                .userAgent("user-agent")
                .refreshTokenJti("old-jti")
                .refreshTokenHash("hashed-current-refresh-token")
                .status(AuthSessionStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build();
        Member member = Member.builder()
                .id(1L)
                .email("member@example.com")
                .name("member")
                .imageUrl("https://example.com/profile.png")
                .provider(OAuth2Provider.KAKAO)
                .providerId("provider-id")
                .build();
        FakeRefreshTokenIssuePort refreshTokenIssuePort = new FakeRefreshTokenIssuePort();
        FakeSaveRefreshTokenCachePort saveRefreshTokenCachePort = new FakeSaveRefreshTokenCachePort();
        FakeSaveAuthSessionPort saveAuthSessionPort = new FakeSaveAuthSessionPort();
        FakeTokenIssuePort tokenIssuePort = new FakeTokenIssuePort();
        TokenService tokenService = new TokenService(
                refreshTokenIssuePort,
                sessionId -> "hashed-current-refresh-token",
                saveRefreshTokenCachePort,
                new FakeRefreshTokenHashPort(),
                sessionId -> Optional.of(authSession),
                saveAuthSessionPort,
                tokenIssuePort,
                new FakeLoadMemberPort(member)
        );

        ReissueTokenResponse response = tokenService.reIssueToken(
                new ReIssueTokenRequest("current-refresh-token")
        );

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.accessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(response.refreshTokenExpiresIn()).isEqualTo(1209600L);
        assertThat(tokenIssuePort.issuedMember).isEqualTo(member);
        assertThat(tokenIssuePort.issuedSessionId).isEqualTo("session-id");
        assertThat(tokenIssuePort.issuedRefreshTokenJti).isNotBlank();
        assertThat(tokenIssuePort.issuedRefreshTokenJti).isNotEqualTo("old-jti");
        assertThat(saveAuthSessionPort.savedAuthSession.getRefreshTokenJti())
                .isEqualTo(tokenIssuePort.issuedRefreshTokenJti);
        assertThat(saveAuthSessionPort.savedAuthSession.getRefreshTokenHash())
                .isEqualTo("hashed-new-refresh-token");
        assertThat(saveRefreshTokenCachePort.savedSessionId).isEqualTo("session-id");
        assertThat(saveRefreshTokenCachePort.savedRefreshTokenHash).isEqualTo("hashed-new-refresh-token");
        assertThat(saveRefreshTokenCachePort.savedTtl).isEqualTo(Duration.ofSeconds(1209600L));
    }

    private static class FakeRefreshTokenIssuePort implements RefreshTokenIssuePort {

        @Override
        public String issueRefreshToken(Long memberId, String sessionId, String refreshTokenJti) {
            return "new-refresh-token";
        }

        @Override
        public RefreshTokenClaims authenticateRefreshToken(String refreshToken) {
            return new RefreshTokenClaims(1L, "session-id", "old-jti");
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

    private static class FakeSaveAuthSessionPort implements SaveAuthSessionPort {

        private AuthSession savedAuthSession;

        @Override
        public AuthSession save(AuthSession authSession) {
            this.savedAuthSession = authSession;
            return authSession;
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
            return Optional.of(member).filter(value -> value.getId().equals(id));
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
