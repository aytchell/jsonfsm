package com.github.aytchell.feedbackstates.exceptions;

public class MalformedInputException extends Exception {
    public MalformedInputException(String message) {
        super(message);
    }

    public MalformedInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
