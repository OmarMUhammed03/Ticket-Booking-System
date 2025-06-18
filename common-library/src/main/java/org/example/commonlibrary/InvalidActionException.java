package org.example.commonlibrary;

public class InvalidActionException extends RuntimeException {
    public InvalidActionException(final String message) {
        super(message);
    }
}

