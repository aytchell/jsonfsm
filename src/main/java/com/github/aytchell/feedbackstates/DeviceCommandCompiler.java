package com.github.aytchell.feedbackstates;

import com.github.aytchell.feedbackstates.exceptions.CompilationException;

public interface DeviceCommandCompiler {
    DeviceCommand compile(String commandString) throws CompilationException;
}
