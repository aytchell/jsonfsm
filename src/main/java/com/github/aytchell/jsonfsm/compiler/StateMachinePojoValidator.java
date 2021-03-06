package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.input.pojos.*;
import com.github.aytchell.validator.Validator;
import com.github.aytchell.validator.exceptions.ValidationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class StateMachinePojoValidator {
    private final StateMachinePojo stateMachinePojo;
    private final Set<String> knownStateNames = new HashSet<>();
    private final Set<String> knownTriggerNames = new HashSet<>();

    private StateMachinePojoValidator(StateMachinePojo stateMachinePojo) {
        this.stateMachinePojo = stateMachinePojo;
    }

    public static void validate(StateMachinePojo pojo) throws ValidationException {
        new StateMachinePojoValidator(pojo).validate();
    }

    private void validate() throws ValidationException {
        validateTriggers();

        // The following method needs the list of all trigger names which is compiled
        // during 'validateTriggers' so we need to run it in that order.
        validateStates();

        // we're checking if initialState and finalStates are known states; so these
        // checks must be done *after* we validated the structure of 'states'
        validateInitialState();
        validateFinalStates();
    }

    private void validateTriggers() throws ValidationException {
        final List<TriggerPojo> allTriggers = stateMachinePojo.getTriggers();
        Validator.expect(allTriggers, "triggers").notNull().notEmpty()
                .eachCustomEntry(
                        trigger -> {
                            Validator.expect(trigger.getName(), "name").notNull().notBlank();
                            Validator.expect(trigger.getEventSourceId(), "eventSourceId").notNull().greaterThan(0);
                            Validator.expect(trigger.getEventPayload(), "eventPayload").notNull().notBlank();
                            knownTriggerNames.add(trigger.getName());
                        }
                )
                // trigger names shall be unique
                .allEntriesAreUnique(
                        StateMachinePojoValidator::triggerNamesAreEqual, "name", TriggerPojo::getName)
                // trigger contents shall be unique
                .allEntriesAreUnique(
                        StateMachinePojoValidator::triggerContentsAreEqual,
                        "(eventSourceId/eventPayload)",
                        t-> "(" + t.getEventSourceId() + "/" + t.getEventPayload() + ")");
    }

    private static boolean triggerNamesAreEqual(TriggerPojo lhs, TriggerPojo rhs) {
        return lhs.getName().equals(rhs.getName());
    }

    private static boolean triggerContentsAreEqual(TriggerPojo lhs, TriggerPojo rhs) {
        return lhs.getEventSourceId().equals(rhs.getEventSourceId()) &&
                lhs.getEventPayload().equals(rhs.getEventPayload());
    }

    private void validateStates() throws ValidationException {
        final List<StatePojo> allStates = stateMachinePojo.getStates();
        Validator.expect(allStates, "states").notNull().notEmpty().eachCustomEntry(
                state -> {
                    Validator.expect(state.getName(), "name").notNull().notBlank();
                    expectBehaviorListIsCompleteIfGiven(state.getOnEntry(), "onEntry");
                    expectBehaviorListIsCompleteIfGiven(state.getOnExit(), "onExit");

                    // For validating the transitions we need to have a list of all known state names
                    knownStateNames.add(state.getName());
                })
                .allEntriesAreUnique(StateMachinePojoValidator::stateNamesAreEqual,
                        "name", StatePojo::getName);

        // the validation step above compiles a list of known states. When validating the transitions we need to have
        // a complete list of known states that's why we need a separate loop over all the states
        Validator.expect(allStates, "states").notNull().notEmpty().eachCustomEntry(
                state -> {
                    Validator.expect(state.getTransitions(), "transitions")
                            .ifNotNull()
                            .eachCustomEntry(this::validateTransition)
                            .allEntriesAreUnique(StateMachinePojoValidator::transitionTriggerNamesAreEqual,
                                    "triggerName", TransitionPojo::getTriggerName);
                });
    }

    private static boolean stateNamesAreEqual(StatePojo lhs, StatePojo rhs) {
        return lhs.getName().equals(rhs.getName());
    }

    private static boolean transitionTriggerNamesAreEqual(TransitionPojo lhs, TransitionPojo rhs) {
        return lhs.getTriggerName().equals(rhs.getTriggerName());
    }

    private void validateInitialState() throws ValidationException {
        Validator.expect(stateMachinePojo.getInitialState(), "initialState")
                .notNull().notBlank()
                .passes(knownStateNames::contains, "is contained in states");
    }

    private void validateFinalStates() throws ValidationException {
        Validator.expect(stateMachinePojo.getFinalStates(), "finalStates")
                .ifNotNull()
                .eachCustomEntry(
                        state -> Validator.expect(state)
                                .notNull().notBlank().passes(knownStateNames::contains, "is contained in states")
                );
    }

    private void validateTransition(TransitionPojo transition) throws ValidationException {
        Validator.expect(transition.getTriggerName(), "triggerName").notNull().notBlank()
                .passes(knownTriggerNames::contains, "is a known triggerName");
        Validator.expect(transition.getTargetState(), "targetState",
                        "alternatively add 'transition.ignore = true'").ifNotGivenOrFalse(transition.getIgnore())
                // if 'ignore' is given and 'true' we skip the test. Otherwise, continue with the check
                .notNull();
        Validator.expect(transition.getTargetState(), "targetState").ifNotGivenOrFalse(transition.getIgnore())
                // no extraInfo if the name is malformed
                .notNull().notBlank().passes(knownStateNames::contains, "is contained in states");
        Validator.expect(transition.getEffects(), "effects").ifNotGivenOrFalse(transition.getIgnore())
                .ifNotNull().eachCustomEntry(this::expectBehaviorIsComplete);
        Validator.expect(transition.getIgnore(), "ignore")
                .ifTrue(transition.getTargetState() == null)
                // no 'targetState' given so there has to be 'ignore' and it must be 'true'
                .notNull().isTrue();
    }

    private void expectBehaviorListIsCompleteIfGiven(List<BehaviorPojo> behaviors, String name)
            throws ValidationException {
        Validator.expect(behaviors, name).ifNotNull().eachCustomEntry(this::expectBehaviorIsComplete);
    }

    private void expectBehaviorIsComplete(BehaviorPojo behavior) throws ValidationException {
        Validator.expect(behavior.getDeviceId(), "deviceId").notNull().greaterThan(0);
        Validator.expect(behavior.getCommandString(), "commandString").notNull().notBlank();
    }
}
