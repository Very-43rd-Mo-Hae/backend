package com.very.relink.auth.adapter.in.security;

import com.very.relink.auth.exception.AuthErrorCode;
import com.very.relink.core.exception.DomainException;
import com.very.relink.core.presentation.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        DomainException exception = AuthErrorCode.ACCESS_DENIED.toException();

        response.setStatus(exception.getHttpStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                ErrorResponse.createDomainErrorResponse()
                        .statusCode(exception.getHttpStatus().value())
                        .exception(exception)
                        .build()
        );
    }
}
