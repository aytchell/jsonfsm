package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;
import com.github.aytchell.feedbackstates.input.pojos.StateMachinePojo;

public class StateMachinePojoValidator {
    private final StateMachinePojo stateMachinePojo;

    public static void validate(StateMachinePojo pojo) throws MalformedInputException {
        new StateMachinePojoValidator(pojo).validate();
    }

    private StateMachinePojoValidator(StateMachinePojo stateMachinePojo) {
        this.stateMachinePojo = stateMachinePojo;
    }

    private void validate() throws MalformedInputException {
        throwIfOptionsAreMissing();
        throwIfInitialStateIsMissing();
        throwIfTriggersAreMissing();
        throwIfStatesAreMissing();
    }

    private void throwIfOptionsAreMissing() throws MalformedInputException {
        if (stateMachinePojo.getOptions() == null) {
            throw new MalformedInputException("StateMachine contains no options");
        }
    }

    private void throwIfInitialStateIsMissing() throws MalformedInputException {
        if (stateMachinePojo.getOptions().getInitialState() == null) {
            throw new MalformedInputException("StateMachine contains no initial state");
        }
    }

    private void throwIfTriggersAreMissing() throws MalformedInputException {
        if (stateMachinePojo.getTriggers() == null || stateMachinePojo.getTriggers().isEmpty()) {
            throw new MalformedInputException("StateMachine contains no triggers");
        }
    }

    private void throwIfStatesAreMissing() throws MalformedInputException {
        if (stateMachinePojo.getStates() == null || stateMachinePojo.getStates().isEmpty()) {
            throw new MalformedInputException("StateMachine contains no states");
        }
    }
}
