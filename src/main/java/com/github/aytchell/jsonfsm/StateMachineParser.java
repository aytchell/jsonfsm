package com.github.aytchell.jsonfsm;

import com.github.aytchell.jsonfsm.compiler.StateMachineParserImpl;
import com.github.aytchell.validator.exceptions.ValidationException;

/**
 * Parser for reading json encoded state machines.
 *
 * This class carries the main entry point for the library. It will parse and validate
 * a given json-encoded string and return a StateMachineCompiler which then can be
 * used to create the StateMachine instance.
 */
public class StateMachineParser {
    /**
     * Parse (and validate) a given json-encoded state machine
     *
     * @param jsonDescription a json-encoded string describing a state machine. Please consult the README.md
     *                        for a description of the accepted format.
     * @return a StateMachine compiler which will insert the commands to be executed by the state machine
     * @throws ValidationException thrown if something with the given state machine description is wrong
     */
    public static StateMachineCompiler parse(String jsonDescription)
            throws ValidationException {
        final StateMachineParserImpl impl = new StateMachineParserImpl();
        return impl.parse(jsonDescription);
    }
}
