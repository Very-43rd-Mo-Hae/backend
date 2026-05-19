package com.very.relink.auth.adapter.out.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.domain.token.AuthenticatedMember;
import com.very.relink.auth.infra.token.JwtProperties;
import com.very.relink.core.exception.DomainException;
import com.very.relink.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenIssueAdapterTest {

    private static final String SECRET = "test-jwt-secret-key-must-be-long-enough";

    @Test
    @DisplayName("JWT 토큰 발급 및 인증 테스트")
    void issueAndAuthenticateAccessToken() {
        JwtTokenIssueAdapter adapter = new JwtTokenIssueAdapter(new JwtProperties(SECRET, 3600L));
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("tester")
                .imageUrl("https://example.com/profile.png")
                .build();

        AuthTokens authTokens = adapter.issue(member);
        AuthenticatedMember authenticatedMember = adapter.authenticate(authTokens.accessToken());

        assertThat(authTokens.tokenType()).isEqualTo("Bearer");
        assertThat(authTokens.expiresIn()).isEqualTo(3600L);
        assertThat(authenticatedMember.memberId()).isEqualTo(1L);
        assertThat(authenticatedMember.email()).isEqualTo("test@example.com");
        assertThat(authenticatedMember.name()).isEqualTo("tester");
    }

    @Test
    @DisplayName("만료된 JWT 토큰 인증 실패 테스트")
    void authenticateExpiredAccessToken() {
        JwtTokenIssueAdapter adapter = new JwtTokenIssueAdapter(new JwtProperties(SECRET, -1L));
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("tester")
                .build();

        AuthTokens authTokens = adapter.issue(member);

        assertThatThrownBy(() -> adapter.authenticate(authTokens.accessToken()))
                .isInstanceOf(DomainException.class)
                .hasMessage("만료된 JWT 토큰입니다.");
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰 인증 실패 테스트")
    void authenticateInvalidAccessToken() {
        JwtTokenIssueAdapter adapter = new JwtTokenIssueAdapter(new JwtProperties(SECRET, 3600L));

        assertThatThrownBy(() -> adapter.authenticate("invalid-token"))
                .isInstanceOf(DomainException.class)
                .hasMessage("유효하지 않은 JWT 토큰입니다.");
    }
}
