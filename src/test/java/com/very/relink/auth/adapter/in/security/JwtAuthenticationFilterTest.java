package com.very.relink.auth.adapter.in.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.very.relink.auth.application.port.out.TokenAuthenticationPort;
import com.very.relink.auth.domain.token.AuthenticatedMember;
import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Bearer 토큰이 유효한 경우의 인증 테스트")
    void setAuthenticationWhenBearerTokenIsValid() throws Exception {
        TokenAuthenticationPort tokenAuthenticationPort = accessToken ->
                new AuthenticatedMember(1L, "test@example.com", "tester");
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                tokenAuthenticationPort,
                JsonMapper.builder().build()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (servletRequest, servletResponse) -> chainCalled.set(true);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(chainCalled).isTrue();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(new AuthenticatedMember(
                1L,
                "test@example.com",
                "tester"
        ));
    }

    @Test
    @DisplayName("Bearer 토큰이 존재하지 않을 때 인증 스킵 테스트")
    void skipAuthenticationWhenBearerTokenDoesNotExist() throws Exception {
        TokenAuthenticationPort tokenAuthenticationPort = accessToken ->
                new AuthenticatedMember(1L, "test@example.com", "tester");
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                tokenAuthenticationPort,
                JsonMapper.builder().build()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain = (servletRequest, servletResponse) -> chainCalled.set(true);

        filter.doFilter(request, response, filterChain);

        assertThat(chainCalled).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
