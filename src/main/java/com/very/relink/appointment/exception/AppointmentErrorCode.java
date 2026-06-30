package com.very.relink.appointment.exception;

import com.very.relink.core.exception.DomainException;
import com.very.relink.core.exception.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppointmentErrorCode implements BaseErrorCode<DomainException> {

    INVALID_APPOINTMENT_TIME(HttpStatus.BAD_REQUEST, "Appointment time must be aligned to 30 minutes and stay within one day."),
    INVALID_PARTICIPANTS(HttpStatus.BAD_REQUEST, "Appointment participants must not be empty."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found."),
    FRIEND_NOT_FOUND(HttpStatus.BAD_REQUEST, "Participant must be an accepted friend."),
    PARTICIPANT_UNAVAILABLE(HttpStatus.CONFLICT, "Participant is unavailable at the requested time.");

    private final HttpStatus httpStatus;
    private final String message;

    AppointmentErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public DomainException toException() {
        return new DomainException(httpStatus, this);
    }
}
