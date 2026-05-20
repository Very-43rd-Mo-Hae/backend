package com.very.relink.auth.adapter.in.security;

import com.very.relink.auth.application.port.out.TokenAuthenticationPort;
import com.very.relink.auth.domain.token.AuthenticatedMember;
import com.very.relink.core.exception.DomainException;
import com.very.relink.core.presentation.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenAuthenticationPort tokenAuthenticationPort;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            TokenAuthenticationPort tokenAuthenticationPort,
            ObjectMapper objectMapper
    ) {
        this.tokenAuthenticationPort = tokenAuthenticationPort;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AuthenticatedMember authenticatedMember = tokenAuthenticationPort.authenticate(accessToken);
            CustomUserDetail userDetail = new CustomUserDetail(authenticatedMember);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetail,
                    null,
                    userDetail.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (DomainException exception) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, exception);
        }
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorization.substring(BEARER_PREFIX.length());
    }

    private void writeErrorResponse(HttpServletResponse response, DomainException exception) throws IOException {
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
