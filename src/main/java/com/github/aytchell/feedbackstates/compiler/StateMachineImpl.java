package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.StateMachine;

public class StateMachineImpl implements StateMachine {
    private final com.github.oxo42.stateless4j.StateMachine<String, String> stateMachine;
    private final TriggerTranslator mapping;

    public StateMachineImpl(com.github.oxo42.stateless4j.StateMachine<String, String> stateMachine,
            TriggerTranslator mapping) {
        this.stateMachine = stateMachine;
        this.mapping = mapping;
    }

    @Override
    public void inject(int eventSourceId, String eventPayload) {
        final String triggerName = mapping.getTriggerName(eventSourceId, eventPayload);
        stateMachine.fire(triggerName);
    }
}
