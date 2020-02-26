package com.github.aytchell.feedbackstates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;

public interface StateMachineParser {
    StateMachineCompiler parseAndListRequiredDeviceIds(String jsonDescription) throws MalformedInputException;
}
