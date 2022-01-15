package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.DeviceCommand;
import com.github.aytchell.jsonfsm.DeviceCommandCompiler;
import com.github.aytchell.jsonfsm.StateMachine;
import com.github.aytchell.jsonfsm.StateMachineCompiler;
import com.github.aytchell.jsonfsm.StateMachineParser;
import com.github.aytchell.jsonfsm.CompilationException;
import com.github.aytchell.validator.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.github.aytchell.jsonfsm.compiler.ExceptionMessageChecks.readResourceTextFile;
import static org.junit.jupiter.api.Assertions.*;

public class StateMachineCompileAndRunTest {
    @Test
    void commandOnExitAndEnter() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("simple_exit_enter.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
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
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
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
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
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
    void multipleEffectsOnTransition() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("multiple_effects.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());
        assertEquals(Set.of(10), compiler.getRequiredDevices());
        assertEquals(Set.of(1), compiler.getAcceptedEventSources());

        StringBuffer buffer = new StringBuffer();

        final StateMachine stateMachine = compiler.compileStateMachine(
                Map.of(10, new LogDeviceCommandCompiler("", buffer)));
        assertEquals(Set.of(10), stateMachine.getControlledDeviceIds());
        assertEquals(Set.of(1), stateMachine.getHandledEventSourceIds());

        stateMachine.injectEvent(1, "move ya");
        assertEquals("Moving to 'Stop' ...Still moving ...", buffer.toString());
    }

    @Test
    void notEnoughDeviceCompilersGivenThrows() throws IOException, ValidationException {
        final String json = readResourceTextFile("multiple_devices_and_cmds.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);

        final Map<Integer, DeviceCommandCompiler> compilers = new HashMap<>();
        compilers.put(1, new LogDeviceCommandCompiler("", new StringBuffer()));
        final Exception e = assertThrows(CompilationException.class, () -> compiler.compileStateMachine(compilers));
        final String message = e.getMessage();
        assertTrue(message.contains("compiler for commands of device"), "Failed message: " + message);
        assertTrue(message.contains("missing"), "Failed message: " + message);
    }

    @Test
    void superfluousIgnoreIsIgnored() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("ignore_false.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
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
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
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

    @Test
    void returnValueReflectsFinalStates() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("final_states.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertTrue(devices.isEmpty());

        final StateMachine stateMachine = compiler.compileStateMachine(Map.of());
        // transition "One" --> "Two"
        final boolean isStateTwoFinal = stateMachine.injectEvent(1, "move ya");
        assertFalse(isStateTwoFinal);
        // transition "Two" --> "Three"
        final boolean isStateThreeFinal = stateMachine.injectEvent(1, "move ya");
        assertTrue(isStateThreeFinal);
        // transition "Three" --> "One"
        final boolean isStateOneFinal = stateMachine.injectEvent(1, "move ya");
        assertTrue(isStateOneFinal);
        // transition "One" --> "Two"
        final boolean isStateTwoNowFinal = stateMachine.injectEvent(1, "move ya");
        assertFalse(isStateTwoNowFinal);
    }

    @Test
    void currentStateIsCorrect() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("final_states.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);

        final StateMachine stateMachine = compiler.compileStateMachine(Map.of());
        // transition "One" --> "Two"
        stateMachine.injectEvent(1, "move ya");
        assertEquals("Two", stateMachine.getCurrentState());
        // transition "Two" --> "Three"
        stateMachine.injectEvent(1, "move ya");
        assertEquals("Three", stateMachine.getCurrentState());
        // transition "Three" --> "One"
        stateMachine.injectEvent(1, "move ya");
        assertEquals("One", stateMachine.getCurrentState());
        // transition "One" --> "Two"
        stateMachine.injectEvent(1, "move ya");
        assertEquals("Two", stateMachine.getCurrentState());
    }

    @Test
    void unknownEventIsIgnored() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("final_states.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertTrue(devices.isEmpty());

        final StateMachine stateMachine = compiler.compileStateMachine(Map.of());
        // transition "One" --> "Two"
        final boolean isStateTwoFinal = stateMachine.injectEvent(1, "move ya");
        assertFalse(isStateTwoFinal);

        // ignore unknown events
        stateMachine.injectEvent(2, "move ya");
        stateMachine.injectEvent(1, "move me");

        // transition "Two" --> "Three"
        final boolean isStateThreeFinal = stateMachine.injectEvent(1, "move ya");
        assertTrue(isStateThreeFinal);

        // ignore unknown events
        stateMachine.injectEvent(2, "move ya");
        final boolean isStillFinal = stateMachine.injectEvent(1, "move me");
        assertTrue(isStillFinal);
    }

    @Test
    void behaviorsOnSelfTransitionAreExecuted() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("self_transition.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertEquals(1, devices.size());
        assertTrue(devices.contains(10));

        StringBuffer buffer = new StringBuffer();
        final StateMachine stateMachine = compiler.compileStateMachine(
                Map.of(10, new LogDeviceCommandCompiler("", buffer)));
        stateMachine.injectEvent(1, "move ya");
        assertEquals("LeavingEntering", buffer.toString());
    }

    @Test
    void effectsOnSelfTransitionAreExecuted() throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("self_transition_effect.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);
        assertNotNull(compiler.getRequiredDevices());

        final Set<Integer> devices = compiler.getRequiredDevices();
        assertEquals(1, devices.size());
        assertTrue(devices.contains(10));

        StringBuffer buffer = new StringBuffer();
        final StateMachine stateMachine = compiler.compileStateMachine(
                Map.of(10, new LogDeviceCommandCompiler("", buffer)));
        stateMachine.injectEvent(1, "move ya");
        assertEquals("Not moving ... ", buffer.toString());
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
