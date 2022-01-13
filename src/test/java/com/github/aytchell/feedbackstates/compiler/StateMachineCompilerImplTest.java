package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.DeviceCommand;
import com.github.aytchell.feedbackstates.DeviceCommandCompiler;
import com.github.aytchell.feedbackstates.StateMachine;
import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.StateMachineParser;
import com.github.aytchell.feedbackstates.exceptions.CompilationException;
import com.github.aytchell.validator.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateMachineCompilerImplTest {
    @Test
    void commandOnExitAndEnter() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("simple_exit_enter.json");
        final StateMachineCompiler compiler = StateMachineParser.parseAndListRequiredDeviceIds(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertEquals(1, devices.size());
        assertTrue(devices.contains(10));

        StringBuffer buffer = new StringBuffer();
        final StateMachine stateMachine = compiler.compileStateMachine(
                Map.of(10, new LogDeviceCommandCompiler("", buffer)));
        stateMachine.injectEvent(1, "move ya");
        assertEquals("Exiting 'Start' ...Entering 'Stop' ...", buffer.toString());
    }

    @Test
    void commandOnTransition() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("effect_on_transition.json");
        final StateMachineCompiler compiler = StateMachineParser.parseAndListRequiredDeviceIds(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertEquals(1, devices.size());
        assertTrue(devices.contains(10));

        StringBuffer buffer = new StringBuffer();
        final StateMachine stateMachine = compiler.compileStateMachine(
                Map.of(10, new LogDeviceCommandCompiler("", buffer)));
        stateMachine.injectEvent(1, "move ya");
        assertEquals("Moving to 'Stop' ...", buffer.toString());
    }

    @Test
    void multipleDevicesAndCommands() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("multiple_devices_and_cmds.json");
        final StateMachineCompiler compiler = StateMachineParser.parseAndListRequiredDeviceIds(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());
        assertEquals(Set.of(1, 2, 3, 4), compiler.getRequiredDevices());
        assertEquals(Set.of(1, 2), compiler.getAcceptedEventSources());

        StringBuffer buffer = new StringBuffer();

        final Map<Integer, DeviceCommandCompiler> compilers = new HashMap<>();
        compilers.put(1, new LogDeviceCommandCompiler("1:", buffer));
        compilers.put(2, new LogDeviceCommandCompiler("2:", buffer));
        compilers.put(3, new LogDeviceCommandCompiler("3:", buffer));
        compilers.put(4, new LogDeviceCommandCompiler("4:", buffer));
        final StateMachine stateMachine = compiler.compileStateMachine(compilers);
        assertEquals(Set.of(1, 2, 3, 4), stateMachine.getControlledDeviceIds());
        assertEquals(Set.of(1, 2), stateMachine.getHandledEventSourceIds());

        stateMachine.injectEvent(1, "move ya");
        assertEquals("1:Cmd1 2:Cmd2 3:Cmd3 4:Cmd4 ", buffer.toString());
    }

    @Test
    void notEnoughDeviceCompilersGivenThrows() throws IOException, ValidationException {
        final String json = readResourceTextFile("multiple_devices_and_cmds.json");
        final StateMachineCompiler compiler = StateMachineParser.parseAndListRequiredDeviceIds(json);

        final Map<Integer, DeviceCommandCompiler> compilers = new HashMap<>();
        compilers.put(1, new LogDeviceCommandCompiler("", new StringBuffer()));
        final Exception e = assertThrows(CompilationException.class, () -> compiler.compileStateMachine(compilers));
        final String message = e.getMessage();
        assertTrue(message.contains("compiler for commands of device"), "Failed message: " + message);
        assertTrue(message.contains("missing"), "Failed message: " + message);
    }

    @Test
    void exceptionFromDeviceCommandCompilerIsForwarded() throws IOException, ValidationException {
        final String json = readResourceTextFile("simple_exit_enter.json");
        final StateMachineCompiler compiler = StateMachineParser.parseAndListRequiredDeviceIds(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertEquals(1, devices.size());
        assertTrue(devices.contains(10));

        StringBuffer buffer = new StringBuffer();
        assertThrows(CompilationException.class, () -> compiler.compileStateMachine(
                Map.of(10, new BadDeviceCommandCompiler())
        ));
    }

    @Test
    void superfluousIgnoreIsIgnored() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("ignore_false.json");
        final StateMachineCompiler compiler = StateMachineParser.parseAndListRequiredDeviceIds(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertEquals(1, devices.size());
        assertTrue(devices.contains(10));

        StringBuffer buffer = new StringBuffer();
        final StateMachine stateMachine = compiler.compileStateMachine(
                Map.of(10, new LogDeviceCommandCompiler("", buffer)));
        stateMachine.injectEvent(1, "move ya");

        // Ensure that the transition is taken even though there is an 'ignored' entry
        assertEquals("Exit 'Start' Enter 'Stop'", buffer.toString());
    }

    @Test
    void ignoreEntryIsRespected() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("ignore_true.json");
        final StateMachineCompiler compiler = StateMachineParser.parseAndListRequiredDeviceIds(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertEquals(1, devices.size());
        assertTrue(devices.contains(10));

        StringBuffer buffer = new StringBuffer();
        final StateMachine stateMachine = compiler.compileStateMachine(
                Map.of(10, new LogDeviceCommandCompiler("", buffer)));
        stateMachine.injectEvent(1, "move ya");

        // Ensure that the transition is NOT taken (the trigger should be accepted by the state machine but ignored)
        final String content = buffer.toString();
        assertTrue(content.isEmpty(), String.format("Buffer should be empty but is '%s'", content));
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

    private static class BadDeviceCommandCompiler implements DeviceCommandCompiler {
        @Override
        public DeviceCommand compile(String commandString) {
            throw new RuntimeException("Really bad exception");
        }
    }
}
