package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.StateMachineParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.aytchell.feedbackstates.compiler.ExceptionMessageChecks.assertThrowsAndMessageReadsLike;

class StateMachineParserImplTest {
    @Test
    void emptyInputGivenThrows() {
        assertThrowsAndMessageReadsLike(
                () -> StateMachineParser.parseAndListRequiredDeviceIds(null),
                List.of("'jsonStateMachine'", "is not null"));

        assertThrowsAndMessageReadsLike(
                () -> StateMachineParser.parseAndListRequiredDeviceIds(""),
                List.of("'jsonStateMachine'", "is not blank"));
    }
}