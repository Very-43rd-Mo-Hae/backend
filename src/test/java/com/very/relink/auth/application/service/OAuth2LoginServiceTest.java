package com.very.relink.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.very.relink.auth.application.command.OAuth2LoginCommand;
import com.very.relink.auth.application.port.out.RefreshTokenHashPort;
import com.very.relink.auth.application.port.out.SaveAuthSessionPort;
import com.very.relink.auth.application.port.out.SaveRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.TokenIssuePort;
import com.very.relink.auth.application.result.OAuth2LoginResult;
import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.auth.domain.session.AuthSessionStatus;
import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.member.application.port.out.LoadMemberPort;
import com.very.relink.member.application.port.out.SaveMemberPort;
import com.very.relink.member.domain.Member;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuth2LoginServiceTest {

    @Test
    @DisplayName("새 멤버 로그인 시 멤버 id와 토큰을 반환하고 auth session을 저장한다.")
    void loginReturnsSavedMemberIdAndStoresAuthSession() {
        LoadMemberPort loadMemberPort = new FakeLoadMemberPort(Optional.empty());
        SaveMemberPort saveMemberPort = member -> Member.builder()
                .id(1L)
                .email(member.getEmail())
                .name(member.getName())
                .imageUrl(member.getImageUrl())
                .provider(member.getProvider())
                .providerId(member.getProviderId())
                .build();
        TokenIssuePort tokenIssuePort = (member, sessionId, refreshTokenJti) ->
                new AuthTokens("access-token", "refresh-token", "Bearer", 3600L, 1209600L);
        FakeSaveAuthSessionPort saveAuthSessionPort = new FakeSaveAuthSessionPort();
        RefreshTokenHashPort refreshTokenHashPort = new FakeRefreshTokenHashPort();
        FakeSaveRefreshTokenCachePort saveRefreshTokenCachePort = new FakeSaveRefreshTokenCachePort();
        OAuth2LoginService service = new OAuth2LoginService(
                loadMemberPort,
                saveMemberPort,
                tokenIssuePort,
                saveAuthSessionPort,
                refreshTokenHashPort,
                saveRefreshTokenCachePort
        );

        OAuth2LoginResult result = service.login(new OAuth2LoginCommand(
                OAuth2Provider.KAKAO,
                "123456789",
                "kakao@example.com",
                "kakao-user",
                "https://example.com/profile.png",
                "device-uuid",
                "iPhone 15",
                "Mozilla/5.0"
        ));

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.authTokens().accessToken()).isEqualTo("access-token");
        assertThat(saveAuthSessionPort.savedAuthSession).isNotNull();
        assertThat(saveAuthSessionPort.savedAuthSession.getMemberId()).isEqualTo(1L);
        assertThat(saveAuthSessionPort.savedAuthSession.getSessionId()).isNotBlank();
        assertThat(saveAuthSessionPort.savedAuthSession.getDeviceId()).isEqualTo("device-uuid");
        assertThat(saveAuthSessionPort.savedAuthSession.getDeviceName()).isEqualTo("iPhone 15");
        assertThat(saveAuthSessionPort.savedAuthSession.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(saveAuthSessionPort.savedAuthSession.getRefreshTokenJti()).isNotBlank();
        assertThat(saveAuthSessionPort.savedAuthSession.getRefreshTokenHash()).isEqualTo("hashed-refresh-token");
        assertThat(saveAuthSessionPort.savedAuthSession.getStatus()).isEqualTo(AuthSessionStatus.ACTIVE);
        assertThat(saveRefreshTokenCachePort.savedSessionId).isEqualTo(saveAuthSessionPort.savedAuthSession.getSessionId());
        assertThat(saveRefreshTokenCachePort.savedRefreshTokenHash).isEqualTo("hashed-refresh-token");
        assertThat(saveRefreshTokenCachePort.savedTtl).isEqualTo(Duration.ofSeconds(1209600L));
    }

    @Test
    @DisplayName("기존 멤버 로그인 시 기존 id를 반환하고 auth session을 저장한다.")
    void loginReturnsExistingMemberIdAndStoresAuthSession() {
        Member existingMember = Member.builder()
                .id(2L)
                .email("kakao@example.com")
                .name("kakao-user")
                .imageUrl("https://example.com/profile.png")
                .provider(OAuth2Provider.KAKAO)
                .providerId("123456789")
                .build();
        LoadMemberPort loadMemberPort = new FakeLoadMemberPort(Optional.of(existingMember));
        SaveMemberPort saveMemberPort = member -> {
            throw new IllegalStateException("Existing member should not be saved again.");
        };
        TokenIssuePort tokenIssuePort = (member, sessionId, refreshTokenJti) ->
                new AuthTokens("access-token", "refresh-token", "Bearer", 3600L, 1209600L);
        FakeSaveAuthSessionPort saveAuthSessionPort = new FakeSaveAuthSessionPort();
        RefreshTokenHashPort refreshTokenHashPort = new FakeRefreshTokenHashPort();
        FakeSaveRefreshTokenCachePort saveRefreshTokenCachePort = new FakeSaveRefreshTokenCachePort();
        OAuth2LoginService service = new OAuth2LoginService(
                loadMemberPort,
                saveMemberPort,
                tokenIssuePort,
                saveAuthSessionPort,
                refreshTokenHashPort,
                saveRefreshTokenCachePort
        );

        OAuth2LoginResult result = service.login(new OAuth2LoginCommand(
                OAuth2Provider.KAKAO,
                "123456789",
                "kakao@example.com",
                "kakao-user",
                "https://example.com/profile.png",
                "device-uuid",
                "iPhone 15",
                "Mozilla/5.0"
        ));

        assertThat(result.memberId()).isEqualTo(2L);
        assertThat(result.authTokens().accessToken()).isEqualTo("access-token");
        assertThat(saveAuthSessionPort.savedAuthSession).isNotNull();
        assertThat(saveAuthSessionPort.savedAuthSession.getMemberId()).isEqualTo(2L);
        assertThat(saveAuthSessionPort.savedAuthSession.getDeviceId()).isEqualTo("device-uuid");
        assertThat(saveAuthSessionPort.savedAuthSession.getDeviceName()).isEqualTo("iPhone 15");
        assertThat(saveAuthSessionPort.savedAuthSession.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(saveRefreshTokenCachePort.savedSessionId).isEqualTo(saveAuthSessionPort.savedAuthSession.getSessionId());
    }

    private record FakeLoadMemberPort(Optional<Member> member) implements LoadMemberPort {

        @Override
        public Optional<Member> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<Member> findByProviderAndProviderId(
                OAuth2Provider provider,
                String providerId
        ) {
            return member.filter(value ->
                    value.getProvider() == provider && value.getProviderId().equals(providerId)
            );
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
}
