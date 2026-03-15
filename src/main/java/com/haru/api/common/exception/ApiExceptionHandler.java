package com.haru.api.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        org.springframework.http.HttpStatusCode statusCode = exception.getStatusCode();
        String error = statusCode instanceof org.springframework.http.HttpStatus httpStatus
                ? httpStatus.getReasonPhrase()
                : statusCode.toString();

        return ResponseEntity.status(statusCode)
                .body(new ApiErrorResponse(
                        OffsetDateTime.now(),
                        statusCode.value(),
                        error,
                        exception.getReason(),
                        request.getRequestURI()
                ));
    }
}
