package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;
import com.github.aytchell.feedbackstates.input.pojos.StateMachinePojo;
import com.github.aytchell.feedbackstates.input.pojos.TriggerPojo;

import java.util.List;

public class StateMachinePojoValidator {
    private final StateMachinePojo stateMachinePojo;

    public static void validate(StateMachinePojo pojo) throws MalformedInputException {
        new StateMachinePojoValidator(pojo).validate();
    }

    private StateMachinePojoValidator(StateMachinePojo stateMachinePojo) {
        this.stateMachinePojo = stateMachinePojo;
    }

    private void validate() throws MalformedInputException {
        validateTriggers();
        validateStates();

        // we're checking if options.initialState is a known state; so this check
        // must be done *after* we validated the structure of 'states'
        validateOptions();
    }

    private void validateTriggers() throws MalformedInputException {
        throwIfTriggersAreMissing();
        throwIfAnyTriggerIsIncomplete();
    }

    private void validateStates() throws MalformedInputException {
        throwIfStatesAreMissing();
    }

    private void validateOptions() throws MalformedInputException {
        throwIfOptionsAreMissing();
        throwIfInitialStateIsMissing();
        throwIfInitialStateIsUnknown();
    }

    private void throwIfOptionsAreMissing() throws MalformedInputException {
        if (stateMachinePojo.getOptions() == null) {
            throw new MalformedInputException("StateMachine contains no options");
        }
    }

    private void throwIfInitialStateIsMissing() throws MalformedInputException {
        final String initStateName = stateMachinePojo.getOptions().getInitialState();
        if (initStateName == null) {
            throw new MalformedInputException("StateMachine contains no initial state");
        }
    }

    private void throwIfInitialStateIsUnknown() throws MalformedInputException {
        final String initStateName = stateMachinePojo.getOptions().getInitialState();
        if (stateMachinePojo.getStates().stream().noneMatch(s -> initStateName.equals(s.getName()))) {
            throw new MalformedInputException("Initial state doesn't denote a known state");
        }
    }

    private void throwIfTriggersAreMissing() throws MalformedInputException {
        if (stateMachinePojo.getTriggers() == null || stateMachinePojo.getTriggers().isEmpty()) {
            throw new MalformedInputException("StateMachine contains no triggers");
        }
    }

    private void throwIfAnyTriggerIsIncomplete() throws MalformedInputException {
        List<TriggerPojo> triggers = stateMachinePojo.getTriggers();
        for (TriggerPojo t : triggers) {
            if (t.getName() == null || t.getEventSourceId() == null || t.getEventPayload() == null) {
                throw new MalformedInputException(
                        "Encountered incomplete trigger. " +
                        "A trigger must always contain 'name', 'eventSourceId' and 'eventPayload'.");
            }
        }
    }

    private void throwIfStatesAreMissing() throws MalformedInputException {
        if (stateMachinePojo.getStates() == null || stateMachinePojo.getStates().isEmpty()) {
            throw new MalformedInputException("StateMachine contains no states");
        }
    }
}
