package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.StateMachineParser;
import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateMachinePojoValidatorTest {
    @Test
    void missingOptionsWillThrow() {
        final String message = expectExceptionReturnMessage("options_missing.json");
        assertTrue(message.contains("no options"), "Failed message: " + message);
    }

    @Test
    void missingInitialStateWillThrow() {
        final String message = expectExceptionReturnMessage("initial_state_missing.json");
        assertTrue(message.contains("no initial state"), "Failed message: " + message);
    }

    @Test
    void initialStateDenotesUnkownState() {
        final String message = expectExceptionReturnMessage("unknown_initial_state.json");
        assertTrue(message.contains("Initial state"), "Failed message: " + message);
        assertTrue(message.contains("known state"), "Failed message: " + message);
    }

    @Test
    void missingTriggersWillThrow() {
        final String message = expectExceptionReturnMessage("triggers_missing.json");
        assertTrue(message.contains("no triggers"), "Failed message: " + message);
    }

    @Test
    void emptyTriggersWillThrow() {
        final String message = expectExceptionReturnMessage("triggers_empty.json");
        assertTrue(message.contains("no triggers"), "Failed message: " + message);
    }

    @Test
    void missingStatesWillThrow() {
        final String message = expectExceptionReturnMessage("states_missing.json");
        assertTrue(message.contains("no states"), "Failed message: " + message);
    }

    @Test
    void emptyStatesWillThrow() {
        final String message = expectExceptionReturnMessage("states_empty.json");
        assertTrue(message.contains("no states"), "Failed message: " + message);
    }

    @Test
    void incompleteTriggerWillThrow() {
        final String message = expectExceptionReturnMessage("trigger_incomplete.json");
        assertTrue(message.contains("incomplete trigger"), "Failed message: " + message);
    }

    @Test
    void unnamedStateWillThrow() {
        final String message = expectExceptionReturnMessage("unnamed_state.json");
        assertTrue(message.contains("anonymous state"), "Failed message: " + message);
    }

    @Test
    void incompleteOnEntryWillThrow() {
        final String message = expectExceptionReturnMessage("onentry_incomplete.json");
        assertTrue(message.contains("incomplete onEntry"), "Failed message: " + message);
    }

    @Test
    void incompleteOnExitWillThrow() {
        final String message = expectExceptionReturnMessage("onexit_incomplete.json");
        assertTrue(message.contains("incomplete onExit"), "Failed message: " + message);
    }

    @Test
    void incompleteTransitionWillThrow() {
        final String message = expectExceptionReturnMessage("transition_incomplete.json");
        assertTrue(message.contains("incomplete transition"), "Failed message: " + message);
    }

    @Test
    void unknownTriggerNameWillThrow() {
        final String message = expectExceptionReturnMessage("unknown_trigger_name.json");
        assertTrue(message.contains("triggerName"), "Failed message: " + message);
        assertTrue(message.contains("unknown"), "Failed message: " + message);
    }

    @Test
    void unknownTargetStateWillThrow() {
        final String message = expectExceptionReturnMessage("unknown_target_state.json");
        assertTrue(message.contains("targetState"), "Failed message: " + message);
        assertTrue(message.contains("unknown"), "Failed message: " + message);
    }

    private String expectExceptionReturnMessage(String filename) {
        final Exception e = assertThrows(MalformedInputException.class,
                () -> StateMachineParser.parseAndListRequiredDeviceIds(readResourceTextFile(filename)));
        return e.getMessage();
    }

    private String readResourceTextFile(String filename) throws IOException {
        try (
                final InputStream input = this.getClass().getResourceAsStream(filename);
        ) {
            byte[] content = input.readAllBytes();
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}
