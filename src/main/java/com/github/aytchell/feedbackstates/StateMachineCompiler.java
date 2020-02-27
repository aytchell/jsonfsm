package com.github.aytchell.feedbackstates;

import com.github.aytchell.feedbackstates.exceptions.CompilationException;

import java.util.Map;
import java.util.Set;

public interface StateMachineCompiler {
    Set<Integer> getRequiredDevices();

    StateMachine compileStateMachine(Map<Integer, DeviceCommandCompiler> commandCompilers) throws CompilationException;
}
