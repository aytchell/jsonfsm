package com.github.aytchell.jsonfsm;

import lombok.Getter;

@Getter
public class CompilationException extends Exception {
    private final String location;
    private final int deviceId;
    private final String commandString;

    public CompilationException(String message) {
        this(message, null);
    }

    public CompilationException(String message, Throwable cause) {
        this(message, cause, "unknown", -1, "unknown");
    }

    protected CompilationException(String message, Throwable cause,
                                   String location, int deviceId, String commandString) {
        super(message, cause);
        this.location = location;
        this.deviceId = deviceId;
        this.commandString = commandString;
    }
}
