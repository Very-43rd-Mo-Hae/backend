package com.very.relink.auth.exception;

import com.very.relink.core.exception.DomainException;
import com.very.relink.core.exception.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements BaseErrorCode<DomainException> {

    UNSUPPORTED_OAUTH2_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth2 제공자입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    AuthErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public DomainException toException() {
        return new DomainException(httpStatus, this);
    }
}
