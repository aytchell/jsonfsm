package com.github.aytchell.jsonfsm;

import com.github.aytchell.jsonfsm.exceptions.CompilationException;

import java.util.Map;
import java.util.Set;

public interface StateMachineCompiler {
    Set<Integer> getRequiredDevices();

    Set<Integer> getAcceptedEventSources();

    StateMachine compileStateMachine(Map<Integer, DeviceCommandCompiler> commandCompilers) throws CompilationException;
}
