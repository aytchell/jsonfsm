package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.DeviceCommandCompiler;
import com.github.aytchell.feedbackstates.StateMachine;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class StateMachineCompilerImpl implements StateMachineCompiler {
    @Getter
    private final List<Integer> requiredDevices;

    StateMachineCompilerImpl(List<Integer> requiredDevices) {
        this.requiredDevices = requiredDevices;
    }

    @Override
    public StateMachine compileStateMachine(Map<Integer, DeviceCommandCompiler> commandCompilers) {
        return new StateMachineImpl();
    }
}
