package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.DeviceCommand;
import com.github.aytchell.feedbackstates.DeviceCommandCompiler;
import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.StateMachine;
import com.github.aytchell.feedbackstates.StateMachineParser;
import com.github.aytchell.feedbackstates.exceptions.CompilationException;
import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
    void commandOnExitAndEnter() throws IOException, MalformedInputException, CompilationException {
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
                Map.of(10, new LogDeviceCommandCompiler("", buffer)));
        stateMachine.inject(1, "move ya");
        assertEquals("Exiting 'Start' ...Entering 'Stop' ...", buffer.toString());
    }

    @Test
    void multipleDevicesAndCommands() throws IOException, MalformedInputException, CompilationException {
        final String json = readResourceTextFile("multiple_devices_and_cmds.json");
        final StateMachineParser parser = new StateMachineParserImpl();
        final StateMachineCompiler compiler = parser.parseAndListRequiredDeviceIds(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertEquals(Set.of(1,2,3,4), devices);

        StringBuffer buffer = new StringBuffer();

        final Map<Integer, DeviceCommandCompiler> compilers = new HashMap<>();
        compilers.put(1, new LogDeviceCommandCompiler("1:", buffer));
        compilers.put(2, new LogDeviceCommandCompiler("2:", buffer));
        compilers.put(3, new LogDeviceCommandCompiler("3:", buffer));
        compilers.put(4, new LogDeviceCommandCompiler("4:", buffer));
        final StateMachine stateMachine = compiler.compileStateMachine(compilers);
        stateMachine.inject(1, "move ya");
        assertEquals("1:Cmd1 2:Cmd2 3:Cmd3 4:Cmd4 ", buffer.toString());
    }

    @Test
    void notEnoughDeviceCompilersGivenThrows() throws IOException, MalformedInputException {
        final String json = readResourceTextFile("multiple_devices_and_cmds.json");
        final StateMachineParser parser = new StateMachineParserImpl();
        final StateMachineCompiler compiler = parser.parseAndListRequiredDeviceIds(json);

        final Map<Integer, DeviceCommandCompiler> compilers = new HashMap<>();
        compilers.put(1, new LogDeviceCommandCompiler("", new StringBuffer()));
        final Exception e = assertThrows(CompilationException.class, () -> compiler.compileStateMachine(compilers));
        final String message = e.getMessage();
        assertTrue(message.contains("compiler for commands of device"), "Failed message: " + message);
        assertTrue(message.contains("missing"), "Failed message: " + message);
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
        private final String prefix;
        private final StringBuffer sb;
        private final String text;

        LogDeviceCommand(String customPrefix, StringBuffer buffer, String text) {
            prefix = customPrefix;
            this.sb = buffer;
            this.text = text;
        }

        @Override
        public void execute() {
            sb.append(prefix);
            sb.append(text);
        }
    }

    private static class LogDeviceCommandCompiler implements DeviceCommandCompiler {
        private final String customPrefix;
        private final StringBuffer sb;

        public LogDeviceCommandCompiler(String customPrefix, StringBuffer buffer) {
            this.customPrefix = customPrefix;
            this.sb = buffer;
        }

        @Override
        public DeviceCommand compile(String commandString) {
            return new LogDeviceCommand(customPrefix, sb, commandString);
        }
    }
}