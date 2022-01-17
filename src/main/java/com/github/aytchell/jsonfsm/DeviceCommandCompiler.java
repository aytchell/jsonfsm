package com.github.aytchell.jsonfsm;

/**
 * Interface for compiling a command string to a closure
 *
 * Instances of this interface are required by the {@link StateMachineCompiler}
 * and have to be provided by the user of the library.
 */
public interface DeviceCommandCompiler {
    /**
     * Compile a given command string to a closure acting on a specific device
     *
     * What exactly implementations of this interface do is completely up to the
     * user of the library. It can be an actual hardware device as well as a logger,
     * a REST call or sending stuff to an MQTT broker.
     *
     * The returned instance will be called whenever the corresponding behavior or effect
     * in the state machine is to be executed.
     *
     * @param commandString the string as given in the json-encoded state machine
     *
     * @return An instance capable of executing the command string (how this is done
     *      depends on the type of device)
     * @throws Exception Whatever goes wrong: the {@link StateMachineCompiler} will catch
     *      this exception, extract the message forward it as {@link CompilationException}.
     */
    DeviceCommand compile(String commandString) throws Exception;
}
