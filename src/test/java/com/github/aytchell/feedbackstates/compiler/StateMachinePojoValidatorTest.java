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
        final StateMachineParser parser = new StateMachineParserImpl();

        final Exception e = assertThrows(MalformedInputException.class,
                () -> parser.parseAndListRequiredDeviceIds(readResourceTextFile("options_missing.json")));
        final String message = e.getMessage();
        assertTrue(message.contains("no options"), "Failed message: " + message);
    }

    @Test
    void missingInitialStateWillThrow() {
        final StateMachineParser parser = new StateMachineParserImpl();

        final Exception e = assertThrows(MalformedInputException.class,
                () -> parser.parseAndListRequiredDeviceIds(readResourceTextFile("initial_state_missing.json")));
        final String message = e.getMessage();
        assertTrue(message.contains("no initial state"), "Failed message: " + message);
    }

    @Test
    void missingTriggersWillThrow() {
        final StateMachineParser parser = new StateMachineParserImpl();

        final Exception e = assertThrows(MalformedInputException.class,
                () -> parser.parseAndListRequiredDeviceIds(readResourceTextFile("triggers_missing.json")));
        final String message = e.getMessage();
        assertTrue(message.contains("no triggers"), "Failed message: " + message);
    }

    @Test
    void emptyTriggersWillThrow() {
        final StateMachineParser parser = new StateMachineParserImpl();

        final Exception e = assertThrows(MalformedInputException.class,
                () -> parser.parseAndListRequiredDeviceIds(readResourceTextFile("triggers_empty.json")));
        final String message = e.getMessage();
        assertTrue(message.contains("no triggers"), "Failed message: " + message);
    }

    @Test
    void missingStatesWillThrow() {
        final StateMachineParser parser = new StateMachineParserImpl();

        final Exception e = assertThrows(MalformedInputException.class,
                () -> parser.parseAndListRequiredDeviceIds(readResourceTextFile("states_missing.json")));
        final String message = e.getMessage();
        assertTrue(message.contains("no states"), "Failed message: " + message);
    }

    @Test
    void emptyStatesWillThrow() {
        final StateMachineParser parser = new StateMachineParserImpl();

        final Exception e = assertThrows(MalformedInputException.class,
                () -> parser.parseAndListRequiredDeviceIds(readResourceTextFile("states_empty.json")));
        final String message = e.getMessage();
        assertTrue(message.contains("no states"), "Failed message: " + message);
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
