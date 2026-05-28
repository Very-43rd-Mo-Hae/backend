package com.very.relink.auth.adapter.out.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.domain.token.AuthenticatedMember;
import com.very.relink.auth.domain.token.RefreshTokenClaims;
import com.very.relink.auth.exception.TokenErrorCode;
import com.very.relink.auth.infra.token.JwtProperties;
import com.very.relink.core.exception.DomainException;
import com.very.relink.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenIssueAdapterTest {

    private static final String SECRET = "test-jwt-secret-key-must-be-long-enough";

    @Test
    @DisplayName("JWT token 발급과 accessToken 인증이 이뤄진다.")
    void issueAndAuthenticateAccessToken() {
        JwtTokenIssueAdapter adapter = new JwtTokenIssueAdapter(new JwtProperties(SECRET, 3600L, 1209600L));
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("tester")
                .imageUrl("https://example.com/profile.png")
                .build();

        AuthTokens authTokens = adapter.issue(member);
        AuthenticatedMember authenticatedMember = adapter.authenticate(authTokens.accessToken());

        assertThat(authTokens.tokenType()).isEqualTo("Bearer");
        assertThat(authTokens.refreshToken()).isNotBlank();
        assertThat(authTokens.accessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(authTokens.refreshTokenExpiresIn()).isEqualTo(1209600L);
        assertThat(authenticatedMember.memberId()).isEqualTo(1L);
        assertThat(authenticatedMember.email()).isEqualTo("test@example.com");
        assertThat(authenticatedMember.name()).isEqualTo("tester");
    }

    @Test
    @DisplayName("refreshtoken 발급과 인증이 이뤄진다")
    void issueAndAuthenticateRefreshToken() {
        JwtTokenIssueAdapter adapter = new JwtTokenIssueAdapter(new JwtProperties(SECRET, 3600L, 1209600L));

        String refreshToken = adapter.issueRefreshToken(1L, "session-id", "refresh-token-jti");
        RefreshTokenClaims refreshTokenClaims = adapter.authenticateRefreshToken(refreshToken);

        assertThat(refreshTokenClaims.memberId()).isEqualTo(1L);
        assertThat(refreshTokenClaims.sessionId()).isEqualTo("session-id");
        assertThat(refreshTokenClaims.refreshTokenJti()).isEqualTo("refresh-token-jti");
    }

    @Test
    @DisplayName("만료된 accessToken은 인증 차단")
    void authenticateExpiredAccessToken() {
        JwtTokenIssueAdapter adapter = new JwtTokenIssueAdapter(new JwtProperties(SECRET, -1L, 1209600L));
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("tester")
                .build();

        AuthTokens authTokens = adapter.issue(member);

        assertThatThrownBy(() -> adapter.authenticate(authTokens.accessToken()))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.EXPIRED_ACCESS_TOKEN.getMessage());
    }

    @Test
    @DisplayName("잘못된 토큰 인증은 접근이 불가하다")
    void authenticateInvalidAccessToken() {
        JwtTokenIssueAdapter adapter = new JwtTokenIssueAdapter(new JwtProperties(SECRET, 3600L, 1209600L));

        assertThatThrownBy(() -> adapter.authenticate("invalid-token"))
                .isInstanceOf(DomainException.class)
                .hasMessage(TokenErrorCode.INVALID_ACCESS_TOKEN.getMessage());
    }
}
