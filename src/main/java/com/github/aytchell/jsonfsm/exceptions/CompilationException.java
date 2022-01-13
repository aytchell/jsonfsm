package com.github.aytchell.jsonfsm.exceptions;

public class CompilationException extends Exception {
    public CompilationException(String message) {
        super(message);
    }

    public CompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
