package com.very.relink.core.exception;

import com.very.relink.core.exception.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainException extends RuntimeException {

    private HttpStatus httpStatus;

    private String code;

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public DomainException(HttpStatus httpStatus, BaseErrorCode<?> baseErrorCode) {
        super(baseErrorCode.getMessage());
        this.httpStatus = httpStatus;
        this.code = baseErrorCode.name();
    }
}
