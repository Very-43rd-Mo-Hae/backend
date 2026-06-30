package com.very.relink.member.exception;

import com.very.relink.core.exception.DomainException;
import com.very.relink.core.exception.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements BaseErrorCode<DomainException> {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found."),
    MEMBER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "Member already withdrawn.");

    private final HttpStatus httpStatus;
    private final String message;

    MemberErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public DomainException toException() {
        return new DomainException(httpStatus, this);
    }
}
