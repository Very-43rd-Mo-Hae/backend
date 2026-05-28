package com.very.relink.auth.exception;

import com.very.relink.core.exception.DomainException;
import com.very.relink.core.exception.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TokenErrorCode implements BaseErrorCode<DomainException> {

    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 access token입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 access token입니다."),
    BLACKLISTED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃 처리된 access token입니다."),

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 refresh token입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "refresh token을 찾을 수 없습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.FORBIDDEN, "refresh token이 일치하지 않습니다."),
    REUSED_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "이미 사용된 refresh token입니다."),

    AUTH_SESSION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "인증 세션을 찾을 수 없습니다."),
    AUTH_SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "인증 세션이 만료되었습니다."),
    AUTH_SESSION_REVOKED(HttpStatus.FORBIDDEN, "인증 세션이 철회되었습니다."),
    AUTH_SESSION_LOGGED_OUT(HttpStatus.UNAUTHORIZED, "로그아웃된 인증 세션입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    TokenErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public DomainException toException() {
        return new DomainException(httpStatus, this);
    }
}
