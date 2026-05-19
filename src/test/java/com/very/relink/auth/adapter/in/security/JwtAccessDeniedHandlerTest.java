package com.very.relink.auth.adapter.in.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.json.JsonMapper;

class JwtAccessDeniedHandlerTest {

    @Test
    @DisplayName("권한이 부족한 요청은 403 응답을 반환한다")
    void handleWhenAccessDenied() throws Exception {
        JwtAccessDeniedHandler accessDeniedHandler = new JwtAccessDeniedHandler(
                JsonMapper.builder().build()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(
                request,
                response,
                new AccessDeniedException("Access denied")
        );

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("ACCESS_DENIED");
        assertThat(response.getContentAsString()).contains("접근 권한이 없습니다.");
    }
}
