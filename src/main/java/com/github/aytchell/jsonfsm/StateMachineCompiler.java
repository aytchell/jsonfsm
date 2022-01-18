package com.github.aytchell.jsonfsm;

import java.util.Map;
import java.util.Set;

/**
 * Intermediate step when compiling a state machine from json.
 */
public interface StateMachineCompiler {
    /**
     * Returns a set of device IDs for which {@link DeviceCommandCompiler}s are required.
     * <p>
     * After parsing a given json the library can tell a caller which device IDs are used by the
     * state machine. Actual compilation of the device commands is done in the next step.
     * <p>
     * This method tells the caller for which devices (identified by their IDs) a
     * {@link DeviceCommandCompiler} is required when calling {@link #compileStateMachine}.
     *
     * @return A set of device IDs for which {@link #compileStateMachine} will require
     * a {@link DeviceCommandCompiler}
     */
    Set<Integer> getRequiredDevices();

    /**
     * Returns a set of event source IDs which are mentioned in the json encoded state machine.
     * <p>
     * The set of IDs returned by this method is purely as an information for the caller.
     * Events from these event source IDs will later be accepted by {@link StateMachine#injectEvent}.
     *
     * @return a set of event source IDs used in the json state machine
     */
    Set<Integer> getAcceptedEventSources();

    /**
     * Used to actually create the {@link StateMachine} instance.
     * <p>
     * After parsing and validating the json-encoded state machine this method will create
     * the actual instance. For this it requires a set of {@link DeviceCommandCompiler}s.
     * The only thing that might fail now is, if a {@link DeviceCommandCompiler} can't compile
     * a given command string.
     *
     * @param commandCompilers a map of compiler instances to compile commands for specific devices.
     *                         Each entry of an instance has the appropriate device ID as key.
     * @return a state machine that changes state  and executes commands when events are injected
     * @throws CompilationException thrown if one of the given {@link DeviceCommandCompiler}s fails
     *                              to compile a given command string
     */
    StateMachine compileStateMachine(Map<Integer, DeviceCommandCompiler> commandCompilers) throws CompilationException;
}
