package com.github.aytchell.jsonfsm;

import com.github.aytchell.jsonfsm.exceptions.CompilationException;

public interface DeviceCommandCompiler {
    DeviceCommand compile(String commandString) throws CompilationException;
}
