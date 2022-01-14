package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.DeviceCommand;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceCommandWrapper implements DeviceCommand {
    private final DeviceCommand delegate;

    DeviceCommandWrapper(DeviceCommand delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute() {
        try {
            delegate.execute();
        } catch (Exception e) {
            log.error("Exception while calling execute()");
        }
    }
}
