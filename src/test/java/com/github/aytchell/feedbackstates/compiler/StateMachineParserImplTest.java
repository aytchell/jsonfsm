package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.DeviceCommand;
import com.github.aytchell.feedbackstates.DeviceCommandCompiler;
import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.StateMachine;
import com.github.aytchell.feedbackstates.StateMachineParser;
import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineParserImplTest {
    @Test
    void emptyInputGivenThrows() throws MalformedInputException {
        final StateMachineParser parser = new StateMachineParserImpl();

        assertThrows(MalformedInputException.class, () -> parser.parseAndListRequiredDeviceIds(null));
        assertThrows(MalformedInputException.class, () -> parser.parseAndListRequiredDeviceIds(""));
    }

    @Test
    void commandOnEnteringStartState() throws IOException, MalformedInputException {
        final String json = readResourceTextFile("simple_exit_enter.json");
        final StateMachineParser parser = new StateMachineParserImpl();
        final StateMachineCompiler compiler = parser.parseAndListRequiredDeviceIds(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertEquals(1, devices.size());
        assertTrue(devices.contains(10));

        StringBuffer buffer = new StringBuffer();
        final StateMachine stateMachine = compiler.compileStateMachine(
                Map.of(10, new LogDeviceCommandCompiler(buffer)));
        stateMachine.inject(1, "move ya");
        assertEquals("Exiting 'Start' ...Entering 'Stop' ...", buffer.toString());
    }

    private String readResourceTextFile(String filename) throws IOException {
        try (
                final InputStream input = this.getClass().getResourceAsStream(filename);
        ) {
            byte[] content = input.readAllBytes();
            return new String(content, StandardCharsets.UTF_8);
        }
    }

    private static class LogDeviceCommand implements DeviceCommand {
        private StringBuffer sb;
        private final String text;

        LogDeviceCommand(StringBuffer buffer, String text) {
            this.sb = buffer;
            this.text = text;
        }

        @Override
        public void execute() {
            sb.append(text);
        }
    }

    private static class LogDeviceCommandCompiler implements DeviceCommandCompiler {
        private StringBuffer sb;

        public LogDeviceCommandCompiler(StringBuffer buffer) {
            sb = buffer;
        }

        @Override
        public DeviceCommand compile(String commandString) {
            return new LogDeviceCommand(sb, commandString);
        }
    }
}