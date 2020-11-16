package com.github.aytchell.feedbackstates.compiler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.aytchell.feedbackstates.compiler.ExceptionMessageChecks.parseFileAssertThrowsAndMessageReadsLike;

public class StateMachinePojoValidatorTest {
    @Test
    void missingOptionsWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("options_missing.json",
                List.of("'options'", "is not null"));
    }

    @Test
    void missingInitialStateWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("initial_state_missing.json",
                List.of("'options.initialState'", "is not null"));
    }

    @Test
    void initialStateDenotesUnkownState() {
        parseFileAssertThrowsAndMessageReadsLike("unknown_initial_state.json",
                List.of("'options.initialState'", "value: 'Starting'", "contained in states"));
    }

    @Test
    void missingTriggersWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("triggers_missing.json",
                List.of("'triggers'", "is not null"));
    }

    @Test
    void emptyTriggersWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("triggers_empty.json",
                List.of("'triggers'", "type: List", "is not empty"));
    }

    @Test
    void missingStatesWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("states_missing.json",
                List.of("'states'", "is not null"));
    }

    @Test
    void emptyStatesWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("states_empty.json",
                List.of("'states'", "type: List", "is not empty"));
    }

    @Test
    void incompleteTriggerWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("trigger_incomplete.json",
                List.of("'triggers[0].eventPayload'", "is not null"));
    }

    @Test
    void unnamedStateWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("unnamed_state.json",
                List.of("'states[1].name'", "is not null"));
    }

    @Test
    void incompleteOnEntryWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("onentry_incomplete.json",
                List.of("'states[1].onEntry[0].commandString'", "is not null"));
    }

    @Test
    void incompleteOnExitWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("onexit_incomplete.json",
                List.of("'states[0].onExit[0].deviceId", "is not null"));
    }

    @Test
    void incompleteTransitionWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("transition_incomplete.json",
                List.of("'states[0].transitions[0].targetState'", "alternatively add", "ignore", "is not null"));
    }

    @Test
    void unknownTriggerNameWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("unknown_trigger_name.json",
                List.of("'states[0].transitions[0].triggerName'", "moveya", "is a known triggerName"));
    }

    @Test
    void unknownTargetStateWillThrow() {
        parseFileAssertThrowsAndMessageReadsLike("unknown_target_state.json",
                List.of("'states[0].transitions[0].targetState'", "'Stopped'", "is contained in states"));
    }
}
