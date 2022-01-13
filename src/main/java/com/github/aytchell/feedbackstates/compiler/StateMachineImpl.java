package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.StateMachine;
import lombok.Getter;

import java.util.Set;

class StateMachineImpl implements StateMachine {
    private final com.github.oxo42.stateless4j.StateMachine<String, String> stateMachine;

    private final Set<String> finalStates;

    private final EventTranslator mapping;
    @Getter
    private final Set<Integer> controlledDeviceIds;
    @Getter
    private final Set<Integer> handledEventSourceIds;

    public StateMachineImpl(
            com.github.oxo42.stateless4j.StateMachine<String, String> stateMachine,
            Set<String> finalStates,
            Set<Integer> controlledDeviceIds,
            Set<Integer> handledEventSourceIds,
            EventTranslator mapping) {
        this.stateMachine = stateMachine;
        this.finalStates = finalStates;
        this.mapping = mapping;
        this.controlledDeviceIds = controlledDeviceIds;
        this.handledEventSourceIds = handledEventSourceIds;
    }

    @Override
    public boolean injectEvent(int eventSourceId, String eventPayload) {
        final String eventName = mapping.getEventName(eventSourceId, eventPayload);
        if (eventName == null) {
            throw new IllegalArgumentException("Payload '" + eventPayload + "' from event source " +
                                               eventSourceId + " not known by state machine");
        }
        stateMachine.fire(eventName);

        if (finalStates.isEmpty()) {
            return false;
        }

        final String currentState = stateMachine.getState();
        return finalStates.contains(currentState);
    }
}
