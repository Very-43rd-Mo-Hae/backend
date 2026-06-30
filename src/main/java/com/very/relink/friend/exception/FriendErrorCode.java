package com.very.relink.friend.exception;

import com.very.relink.core.exception.DomainException;
import com.very.relink.core.exception.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FriendErrorCode implements BaseErrorCode<DomainException> {

    INVALID_PAGE_REQUEST(HttpStatus.BAD_REQUEST, "Invalid friend page request."),
    INVALID_STATUS_MEMBER_IDS(HttpStatus.BAD_REQUEST, "Status member ids must contain 1 to 10 ids."),
    INVALID_LIGHTNING_EXPIRES_AT(HttpStatus.BAD_REQUEST, "Lightning expiration time must be in the future."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found.");

    private final HttpStatus httpStatus;
    private final String message;

    FriendErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public DomainException toException() {
        return new DomainException(httpStatus, this);
    }
}
