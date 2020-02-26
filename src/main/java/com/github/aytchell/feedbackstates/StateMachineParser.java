package com.github.aytchell.feedbackstates;

public interface StateMachineParser {
    StateMachineCompiler parseAndListRequiredDeviceIds(String jsonDescription);
}
