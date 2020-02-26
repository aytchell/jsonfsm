package com.github.aytchell.feedbackstates;

public interface StateMachine {
    void inject(int eventSourceId, String eventPayload);
}
