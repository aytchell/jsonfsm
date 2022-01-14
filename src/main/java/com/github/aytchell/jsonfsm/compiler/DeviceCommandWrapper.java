package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.DeviceCommand;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceCommandWrapper implements DeviceCommand {
    private final DeviceCommand delegate;
    private final String location;
    private final int deviceId;
    private final String commandString;

    DeviceCommandWrapper(DeviceCommand delegate, String location, int deviceId, String commandString) {
        this.delegate = delegate;
        this.location = location;
        this.deviceId = deviceId;
        this.commandString = commandString;
    }

    @Override
    public void execute() {
        try {
            delegate.execute();
        } catch (Exception e) {
            log.warn("Invocation of behavior failed: loc '{}', dev {}, cmd '{}', msg '{}'",
                    location, deviceId, commandString, e.getMessage());
        }
    }
}
