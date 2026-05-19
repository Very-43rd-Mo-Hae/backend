package com.very.relink.auth.adapter.in.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import tools.jackson.databind.json.JsonMapper;

class JwtAuthenticationEntryPointTest {

    @Test
    @DisplayName("인증되지 않은 요청은 401 응답을 반환한다")
    void commenceWhenAuthenticationRequired() throws Exception {
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(
                JsonMapper.builder().build()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                request,
                response,
                new AuthenticationCredentialsNotFoundException("Authentication required")
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTHENTICATION_REQUIRED");
        assertThat(response.getContentAsString()).contains("인증이 필요합니다.");
    }
}
