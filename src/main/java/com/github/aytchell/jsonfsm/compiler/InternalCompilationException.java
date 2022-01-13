package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.CompilationException;

public class InternalCompilationException extends CompilationException {
    InternalCompilationException(String message, Throwable cause,
                                 String location, int deviceId, String commandString) {
        super(message, cause, location, deviceId, commandString);
    }
}
