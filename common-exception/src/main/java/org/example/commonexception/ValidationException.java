package org.example.commonexception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}

