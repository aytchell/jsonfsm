package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;
import com.github.aytchell.feedbackstates.input.pojos.CommandPojo;
import com.github.aytchell.feedbackstates.input.pojos.StateMachinePojo;
import com.github.aytchell.feedbackstates.input.pojos.StatePojo;
import com.github.aytchell.feedbackstates.input.pojos.TransitionPojo;
import com.github.aytchell.feedbackstates.input.pojos.TriggerPojo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StateMachinePojoValidator {
    private final StateMachinePojo stateMachinePojo;
    private final Set<String> knownStateNames = new HashSet<>();
    private final Set<String> knownTriggerNames = new HashSet<>();

    private StateMachinePojoValidator(StateMachinePojo stateMachinePojo) {
        this.stateMachinePojo = stateMachinePojo;
    }

    public static void validate(StateMachinePojo pojo) throws MalformedInputException {
        new StateMachinePojoValidator(pojo).validate();
    }

    private void validate() throws MalformedInputException {
        validateTriggers();

        // The following method needs the list of all trigger names which is compiled
        // during 'validateTriggers' so we need to run it in that order.
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
        validateEverySingleState();

        // For validating the transitions we need to have a list of all known state names
        // this list is compiled during validateEverySingleState. That's why we don't check
        // transitions in the later method but have a separate method.
        validateEverySingleTransition();
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
        if (!knownStateNames.contains(initStateName)) {
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
            knownTriggerNames.add(t.getName());
        }
    }

    private void throwIfStatesAreMissing() throws MalformedInputException {
        if (stateMachinePojo.getStates() == null || stateMachinePojo.getStates().isEmpty()) {
            throw new MalformedInputException("StateMachine contains no states");
        }
    }

    private void validateEverySingleState() throws MalformedInputException {
        for (StatePojo s : stateMachinePojo.getStates()) {
            throwIfNameIsMissing(s);
            throwIfOnEntryIsIncomplete(s);
            throwIfOnExitIsIncomplete(s);
        }
    }

    private void validateEverySingleTransition() throws MalformedInputException {
        for (StatePojo s : stateMachinePojo.getStates()) {
            List<TransitionPojo> transitions = s.getTransitions();
            if (transitions == null) {
                // This is a final state. There's no way out.
                continue;
            }
            for (TransitionPojo t : transitions) {
                throwIfTransitionIsIncomplete(t);
                throwIfTargetStateIsUnknown(t);
                throwIfTriggerNameIsUnknown(t);
            }
        }
    }

    private void throwIfTransitionIsIncomplete(TransitionPojo transition) throws MalformedInputException {
        if (transition.getTargetState() == null || transition.getTriggerName() == null) {
            throw new MalformedInputException(
                    "Encountered incomplete transition. " +
                    "A transition must always contain 'targetState' and 'triggerName'.");
        }
    }

    private void throwIfTargetStateIsUnknown(TransitionPojo transition) throws MalformedInputException {
        if (!knownStateNames.contains(transition.getTargetState())) {
            throw new MalformedInputException(
                    "Encountered unknown targetState '" + transition.getTargetState() + "' of transition.");
        }
    }

    private void throwIfTriggerNameIsUnknown(TransitionPojo transition) throws MalformedInputException {
        if (!knownTriggerNames.contains(transition.getTriggerName())) {
            throw new MalformedInputException(
                    "Encountered unknown triggerName '" + transition.getTriggerName() + "' of transition.");
        }
    }

    private void throwIfNameIsMissing(StatePojo state) throws MalformedInputException {
        if (state.getName() == null) {
            throw new MalformedInputException("Encountered anonymous state. Please give every state a name.");
        }
        knownStateNames.add(state.getName());
    }

    private void throwIfOnEntryIsIncomplete(StatePojo state) throws MalformedInputException {
        throwIfCommandListIsIncomplete(state.getOnEntry(), "onEntry");
    }

    private void throwIfOnExitIsIncomplete(StatePojo state) throws MalformedInputException {
        throwIfCommandListIsIncomplete(state.getOnExit(), "onExit");
    }

    private void throwIfCommandListIsIncomplete(List<CommandPojo> commands, String name) throws MalformedInputException {
        if (commands == null) {
            // it's OK to have no command list at all
            return;
        }

        for (CommandPojo cmd : commands) {
            if (cmd.getDeviceId() == null || cmd.getCommandString() == null) {
                throw new MalformedInputException(
                        "Encountered incomplete " + name + " command. " +
                        "An " + name + " must always contain 'deviceId' and 'commandString'.");
            }
        }
    }
}
