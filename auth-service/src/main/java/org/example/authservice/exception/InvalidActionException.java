package org.example.authservice.exception;

public class InvalidActionException extends RuntimeException {
    public InvalidActionException(final String message) {
        super("Invalid action: " + message);
    }
}
