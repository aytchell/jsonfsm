package com.github.aytchell.jsonfsm;

public interface DeviceCommandCompiler {
    DeviceCommand compile(String commandString) throws CompilationException;
}
