package com.github.aytchell.jsonfsm;

/**
 * Interface for executing a command.
 *
 * Instances of this interface have to be produced by instances of
 * {@link StateMachineCompiler} and have to be implemented by the user of the library.
 */
public interface DeviceCommand {
    /**
     * Execute a command as defined in the json-encoded state machine.
     *
     * According to UML a behavior or effect will never fail and finish "quickly".
     * The library will catch and log {@code RuntimeException}s so they won't affect
     * other behaviors or the state machine. The command is executed synchronously.
     */
    void execute();
}
