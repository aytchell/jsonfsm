package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.StateMachineParser;

import java.util.List;

public class StateMachineParserImpl implements StateMachineParser {
    @Override
    public StateMachineCompiler parseAndListRequiredDeviceIds(String jsonDescription) {
        return new StateMachineCompilerImpl(List.of());
    }
}
