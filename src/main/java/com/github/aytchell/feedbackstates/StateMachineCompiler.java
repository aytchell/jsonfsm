package com.github.aytchell.feedbackstates;

import java.util.List;
import java.util.Map;

public interface StateMachineCompiler {
    List<Integer> getRequiredDevices();
    StateMachine compileStateMachine(Map<Integer, DeviceCommandCompiler> commandCompilers);
}
