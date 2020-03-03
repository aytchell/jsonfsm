package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.StateMachineParser;
import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StateMachineParserImplTest {
    @Test
    void emptyInputGivenThrows() throws MalformedInputException {
        final StateMachineParser parser = new StateMachineParserImpl();

        assertThrows(MalformedInputException.class, () -> parser.parseAndListRequiredDeviceIds(null));
        assertThrows(MalformedInputException.class, () -> parser.parseAndListRequiredDeviceIds(""));
    }
}