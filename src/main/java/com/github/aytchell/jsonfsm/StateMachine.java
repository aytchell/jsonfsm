package com.github.aytchell.jsonfsm;

import java.util.Set;

public interface StateMachine {
    boolean injectEvent(int eventSourceId, String eventPayload);

    Set<Integer> getControlledDeviceIds();

    Set<Integer> getHandledEventSourceIds();

    String getCurrentState();
}
