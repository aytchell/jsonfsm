package com.github.aytchell.feedbackstates;

import com.github.aytchell.feedbackstates.compiler.StateMachineParserImpl;
import com.github.aytchell.validator.exceptions.ValidationException;

public class StateMachineParser {
    public static StateMachineCompiler parseAndListRequiredDeviceIds(String jsonDescription)
            throws ValidationException {
        final StateMachineParserImpl impl = new StateMachineParserImpl();
        return impl.parseAndListRequiredDeviceIds(jsonDescription);
    }
}
