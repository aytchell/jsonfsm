package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.*;
import com.github.aytchell.validator.exceptions.ValidationException;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static com.github.aytchell.jsonfsm.compiler.ExceptionMessageChecks.readResourceTextFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// If a given DeviceCommand.execute() throws an exception this should be handled gracefully
public class ExceptionOnExecuteTest {
    @Test
    void exceptionDuringFirstOnExit() throws CompilationException, ValidationException, IOException {
        exceptionInBehavior("Exiting1");
    }

    @Test
    void exceptionDuringSecondOnExit() throws CompilationException, ValidationException, IOException {
        exceptionInBehavior("Exiting2");
    }

    @Test
    void exceptionDuringFirstTransitionEffect() throws IOException, ValidationException, CompilationException {
        exceptionInBehavior("Moving1");
    }

    @Test
    void exceptionDuringSecondTransitionEffect() throws IOException, ValidationException, CompilationException {
        exceptionInBehavior("Moving2");
    }

    @Test
    void exceptionDuringFirstOnEntry() throws CompilationException, ValidationException, IOException {
        exceptionInBehavior("Entering1");
    }

    @Test
    void exceptionDuringSecondOnEntry() throws CompilationException, ValidationException, IOException {
        exceptionInBehavior("Entering2");
    }

    @Test
    void eventWithoutMatchingTransition() throws IOException, CompilationException, ValidationException {
        final String json = readResourceTextFile("effects_everywhere.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);

        EvilDeviceCommandCompiler cmdCompiler = new EvilDeviceCommandCompiler();
        final StateMachine machine = compiler.compileStateMachine(Map.of(5, cmdCompiler));
        machine.injectEvent(3, "fly away");
        // if there's no exception this test is "passed"
    }

    private void exceptionInBehavior(String cmdPrefix) throws IOException, ValidationException, CompilationException {
        final String json = readResourceTextFile("effects_everywhere.json");
        final StateMachineCompiler compiler = StateMachineParser.parse(json);
        assertNotNull(compiler);

        EvilDeviceCommandCompiler cmdCompiler = new EvilDeviceCommandCompiler(cmdPrefix);
        final StateMachine machine = compiler.compileStateMachine(Map.of(5, cmdCompiler));
        // this will trigger a transition with a bad effect
        machine.injectEvent(3, "move ya");
        // no exception should leave the above call and all the commands should have been executed
        // (thrown exception does not interfere with the other effects)
        assertEquals(6, cmdCompiler.getExecCounter());
    }

    private static class EvilDeviceCommandCompiler implements DeviceCommandCompiler {
        final String prefix;
        @Getter
        int execCounter;

        EvilDeviceCommandCompiler() {
            this(null);
        }

        EvilDeviceCommandCompiler(String commandPrefix) {
            this.prefix = commandPrefix;
        }

        @Override
        public DeviceCommand compile(String commandString) {
            if (prefix == null || commandString.startsWith(prefix)) {
                return () -> { ++execCounter; throw new RuntimeException("Booom!"); };
            }
            return () -> ++execCounter;
        }
    }
}
