package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.DeviceCommand;
import com.github.aytchell.feedbackstates.DeviceCommandCompiler;
import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.StateMachine;
import com.github.aytchell.feedbackstates.StateMachineParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineParserImplTest {
    @Test
    void nullAsInputWorks() {
        final StateMachineParser parser = new StateMachineParserImpl();

        final StateMachineCompiler compiler = parser.parseAndListRequiredDeviceIds(null);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());
        assertTrue(compiler.getRequiredDevices().isEmpty());
    }

    @Test
    void nullAsInputGivesNopMachine() {
        final StateMachineParser parser = new StateMachineParserImpl();

        final StateMachineCompiler compiler = parser.parseAndListRequiredDeviceIds(null);
        assertNotNull(compiler);
        final StateMachine stateMachine = compiler.compileStateMachine(Map.of());
        assertNotNull(stateMachine);
    }

    @Test
    void emptyStringAsInputWorks() {
        final StateMachineParser parser = new StateMachineParserImpl();

        final StateMachineCompiler compiler = parser.parseAndListRequiredDeviceIds("");
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());
        assertTrue(compiler.getRequiredDevices().isEmpty());
    }

    @Test
    void emptyStringAsInputGivesNopMachine() {
        final StateMachineParser parser = new StateMachineParserImpl();

        final StateMachineCompiler compiler = parser.parseAndListRequiredDeviceIds("");
        assertNotNull(compiler);
        final StateMachine stateMachine = compiler.compileStateMachine(Map.of());
        assertNotNull(stateMachine);
    }

    @Test
    void commandOnEnteringStartState() throws IOException {
        final String json = readResourceTextFile("print_on_start.json");
        final StateMachineParser parser = new StateMachineParserImpl();
        final StateMachineCompiler compiler = parser.parseAndListRequiredDeviceIds(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final List<Integer> deviceList = compiler.getRequiredDevices();
        assertEquals(1, deviceList.size());
        assertEquals(10, deviceList.get(0));

        StringBuffer buffer = new StringBuffer();
        final StateMachine stateMachine = compiler.compileStateMachine(
                Map.of(10, new LogDeviceCommandCompiler(buffer)));
        assertEquals("Hello, world!", buffer.toString());
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