package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.DeviceCommand;
import com.github.aytchell.jsonfsm.DeviceCommandCompiler;
import com.github.aytchell.jsonfsm.StateMachineCompiler;
import com.github.aytchell.jsonfsm.StateMachineParser;
import com.github.aytchell.jsonfsm.CompilationException;
import com.github.aytchell.validator.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.aytchell.jsonfsm.compiler.ExceptionMessageChecks.assertMessageReadsLike;
import static com.github.aytchell.jsonfsm.compiler.ExceptionMessageChecks.readResourceTextFile;
import static org.junit.jupiter.api.Assertions.*;

// Unit tests to ensure that - in case of an exception from DeviceCommandCompiler - the
// error is properly propagated to the caller
public class CompilationExceptionTest {
    @Test
    void compilationExceptionInTransitionEffect() throws IOException, ValidationException {
        final String json = readResourceTextFile("multiple_effects.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);

        CompilationException exception = assertThrows(
                CompilationException.class,
                () -> compiler.compileStateMachine(
                        Map.of(10, new BrokenDeviceCommandCompiler())));
        assertEquals(10, exception.getDeviceId());
        final String commandString = exception.getCommandString();
        boolean commandAsExpected =
                commandString.equals("Moving to 'Stop' ...") ^ commandString.equals("Still moving ...");
        assertTrue(commandAsExpected);
        assertMessageReadsLike(exception.getLocation(), List.of("transition", "Start", "Stop", "trigger", "move"));
    }

    @Test
    void compilationExceptionOnEntry() throws IOException, ValidationException {
        final String json = readResourceTextFile("simple_exit_enter.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);

        CompilationException exception = assertThrows(
                CompilationException.class,
                () -> compiler.compileStateMachine(
                        Map.of(10, new BrokenDeviceCommandCompiler("Entering"))));
        assertEquals(10, exception.getDeviceId());
        assertEquals("Entering 'Stop' ...", exception.getCommandString());
        assertMessageReadsLike(exception.getLocation(), List.of("onEntry", "Stop"));
    }

    @Test
    void compilationExceptionOnExit() throws IOException, ValidationException {
        final String json = readResourceTextFile("simple_exit_enter.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);

        CompilationException exception = assertThrows(
                CompilationException.class,
                () -> compiler.compileStateMachine(
                        Map.of(10, new BrokenDeviceCommandCompiler("Exiting"))));
        assertEquals(10, exception.getDeviceId());
        assertEquals("Exiting 'Start' ...", exception.getCommandString());
        assertMessageReadsLike(exception.getLocation(), List.of("onExit", "Start"));
    }

    private static class BrokenDeviceCommandCompiler implements DeviceCommandCompiler {
        final String prefix;

        BrokenDeviceCommandCompiler() {
            this(null);
        }

        BrokenDeviceCommandCompiler(String commandPrefix) {
            this.prefix = commandPrefix;
        }

        @Override
        public DeviceCommand compile(String commandString) {
            if (prefix == null || commandString.startsWith(prefix)) {
                //throw new CompilationException("Booom!");
                throw new RuntimeException("Booom!");
            }
            return () -> { };
        }
    }
}
