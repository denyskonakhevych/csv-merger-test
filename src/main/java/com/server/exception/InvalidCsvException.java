package com.server.exception;

public class InvalidCsvException extends RuntimeException {

    public InvalidCsvException(final Throwable cause) {
        super(cause);
    }

    public InvalidCsvException(final String message) {
        super(message);
    }
}
