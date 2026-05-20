package com.very.relink.auth.adapter.in.security;

import com.very.relink.auth.application.service.OAuth2AuthenticatedUser;
import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.core.presentation.RestResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2AuthenticatedUser principal = (OAuth2AuthenticatedUser) authentication.getPrincipal();

        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                new RestResponse<>(OAuth2LoginSuccessResponse.from(
                        principal.getMemberId(),
                        principal.getAuthTokens()
                ))
        );
    }

    private record OAuth2LoginSuccessResponse(
            Long memberId,
            String accessToken,
            String tokenType,
            Long expiresIn
    ) {

        private static OAuth2LoginSuccessResponse from(Long memberId, AuthTokens authTokens) {
            return new OAuth2LoginSuccessResponse(
                    memberId,
                    authTokens.accessToken(),
                    authTokens.tokenType(),
                    authTokens.expiresIn()
            );
        }
    }
}
