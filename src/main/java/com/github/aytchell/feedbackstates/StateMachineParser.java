package com.github.aytchell.feedbackstates;

import com.github.aytchell.feedbackstates.compiler.StateMachineParserImpl;
import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;

public class StateMachineParser {
    public static StateMachineCompiler parseAndListRequiredDeviceIds(String jsonDescription)
            throws MalformedInputException {
        final StateMachineParserImpl impl = new StateMachineParserImpl();
        return impl.parseAndListRequiredDeviceIds(jsonDescription);
    }
}
