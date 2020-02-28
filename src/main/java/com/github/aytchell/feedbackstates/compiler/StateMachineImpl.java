package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.StateMachine;
import lombok.Getter;

import java.util.Set;

public class StateMachineImpl implements StateMachine {
    private final com.github.oxo42.stateless4j.StateMachine<String, String> stateMachine;
    private final TriggerTranslator mapping;
    @Getter
    private final Set<Integer> controlledDeviceIds;
    @Getter
    private final Set<Integer> handledEventSourceIds;

    public StateMachineImpl(
            com.github.oxo42.stateless4j.StateMachine<String, String> stateMachine,
            Set<Integer> controlledDeviceIds,
            Set<Integer> handledEventSourceIds,
            TriggerTranslator mapping) {
        this.stateMachine = stateMachine;
        this.mapping = mapping;
        this.controlledDeviceIds = controlledDeviceIds;
        this.handledEventSourceIds = handledEventSourceIds;
    }

    @Override
    public void inject(int eventSourceId, String eventPayload) {
        final String triggerName = mapping.getTriggerName(eventSourceId, eventPayload);
        stateMachine.fire(triggerName);
    }
}
