package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.StateMachineParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.aytchell.jsonfsm.compiler.ExceptionMessageChecks.assertThrowsAndMessageReadsLike;

class StateMachineParserImplTest {
    @Test
    void emptyInputGivenThrows() {
        assertThrowsAndMessageReadsLike(
                () -> StateMachineParser.parse(null),
                List.of("'jsonStateMachine'", "is not null"));

        assertThrowsAndMessageReadsLike(
                () -> StateMachineParser.parse(""),
                List.of("'jsonStateMachine'", "is not blank"));
    }

    @Test
    void brokenJsonAsInputFailsProperly() {
        assertThrowsAndMessageReadsLike(
                () -> StateMachineParser.parse(
                        "{ \"initialState\" }"),
                List.of("Error while parsing", "Unexpected character"));
    }
}