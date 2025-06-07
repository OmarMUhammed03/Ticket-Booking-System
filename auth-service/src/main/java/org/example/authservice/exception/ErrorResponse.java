package org.example.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String message;
    private final HttpStatus status;
    private final LocalDateTime timestamp;

    public ErrorResponse(final String message, final HttpStatus status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
