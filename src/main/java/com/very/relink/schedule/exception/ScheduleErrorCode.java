package com.very.relink.schedule.exception;

import com.very.relink.core.exception.DomainException;
import com.very.relink.core.exception.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ScheduleErrorCode implements BaseErrorCode<DomainException> {

    INVALID_SCHEDULE_RANGE(HttpStatus.BAD_REQUEST, "Invalid schedule range."),
    INVALID_SCHEDULE_SLOT_UNIT(HttpStatus.BAD_REQUEST, "Schedule slot must be aligned to 30 minutes."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found.");

    private final HttpStatus httpStatus;
    private final String message;

    ScheduleErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public DomainException toException() {
        return new DomainException(httpStatus, this);
    }
}
