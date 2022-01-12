package com.github.aytchell.feedbackstates;

import java.util.Set;

public interface StateMachine {
    void injectEvent(int eventSourceId, String eventPayload);

    Set<Integer> getControlledDeviceIds();

    Set<Integer> getHandledEventSourceIds();
}
