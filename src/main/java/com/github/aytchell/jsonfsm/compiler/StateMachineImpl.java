package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.StateMachine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
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
        if (eventName != null) {
            stateMachine.fire(eventName);
        } else {
            log.info("Ignoring unknown event '{}:{}'", eventSourceId, eventPayload);
        }

        if (finalStates.isEmpty()) {
            return false;
        }

        final String currentState = stateMachine.getState();
        return finalStates.contains(currentState);
    }
}
