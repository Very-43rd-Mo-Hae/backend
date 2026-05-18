package com.very.relink.core.exception;

import com.very.relink.core.presentation.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(
                        ErrorResponse.createValidationErrorResponse()
                                .statusCode(400)
                                .exception(ex)
                                .build()
                );
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        log.warn("Domain error: {}", ex.getMessage());

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(
                        ErrorResponse.createDomainErrorResponse()
                                .statusCode(ex.getHttpStatus().value())
                                .exception(ex)
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Exception: {}", ex.getMessage());

        return ResponseEntity
                .internalServerError()
                .body(
                        ErrorResponse.createErrorResponse()
                                .statusCode(500)
                                .exception(ex)
                                .build()
                );
    }
}
