package com.github.aytchell.jsonfsm;

import lombok.Getter;

/**
 * Exception which is thrown when {@link StateMachineCompiler#compileStateMachine} fails.
 */
@Getter
public class CompilationException extends Exception {
    /**
     * A human-readable string describing "where in the state machine" the problem occurred.
     * <p>
     * This might be something like "onEntry(stateX)" so the author of the json-string
     * knows where to find the problem.
     */
    private final String location;

    /**
     * The device ID (if any) involved into the troubles.
     * <p>
     * If the problem stems from a {@link DeviceCommandCompiler} then this field
     * will contain the device's ID.
     */
    private final int deviceId;

    /**
     * The command string (if any) involved into the troubles.
     * <p>
     * If the problem stems from a {@link DeviceCommandCompiler} then this field
     * will contain the command string of the behavior or effect.
     */
    private final String commandString;

    /**
     * Constructor (used internally be the lib).
     *
     * @param message       a nice descriptive error message
     * @param cause         the causing exception (e.g. from the {@link DeviceCommandCompiler})
     * @param location      a human-readable string describing "where in the state machine" the problem occurred
     * @param deviceId      the device ID (if any) involved into the troubles
     * @param commandString the command string (if any) involved into the troubles
     */
    public CompilationException(String message, Exception cause,
                                String location, int deviceId, String commandString) {
        super(message, cause);
        this.location = location;
        this.deviceId = deviceId;
        this.commandString = commandString;
    }
}
