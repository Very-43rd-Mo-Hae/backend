package com.very.relink.auth.adapter.in.security;

import com.very.relink.auth.exception.AuthErrorCode;
import com.very.relink.core.exception.DomainException;
import com.very.relink.core.presentation.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public OAuth2LoginFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        DomainException domainException = extractDomainException(exception);

        response.setStatus(domainException.getHttpStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                ErrorResponse.createDomainErrorResponse()
                        .statusCode(domainException.getHttpStatus().value())
                        .exception(domainException)
                        .build()
        );
    }

    private DomainException extractDomainException(AuthenticationException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof DomainException domainException) {
                return domainException;
            }
            current = current.getCause();
        }

        return AuthErrorCode.OAUTH2_LOGIN_FAILED.toException();
    }
}
