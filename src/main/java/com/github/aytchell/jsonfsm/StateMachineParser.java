package com.github.aytchell.jsonfsm;

import com.github.aytchell.jsonfsm.compiler.StateMachineParserImpl;
import com.github.aytchell.validator.exceptions.ValidationException;

public class StateMachineParser {
    public static StateMachineCompiler parseAndListRequiredDeviceIds(String jsonDescription)
            throws ValidationException {
        final StateMachineParserImpl impl = new StateMachineParserImpl();
        return impl.parseAndListRequiredDeviceIds(jsonDescription);
    }
}
