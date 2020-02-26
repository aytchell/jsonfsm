package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.DeviceCommand;
import com.github.aytchell.feedbackstates.DeviceCommandCompiler;
import com.github.aytchell.feedbackstates.StateMachine;
import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.input.pojos.CommandPojo;
import com.github.aytchell.feedbackstates.input.pojos.StateMachinePojo;
import com.github.aytchell.feedbackstates.input.pojos.StatePojo;
import com.github.aytchell.feedbackstates.input.pojos.TransitionPojo;
import com.github.oxo42.stateless4j.StateConfiguration;
import com.github.oxo42.stateless4j.StateMachineConfig;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StateMachineCompilerImpl implements StateMachineCompiler {
    @Getter
    private final Set<Integer> requiredDevices;
    private final TriggerTranslator mapping;
    private final StateMachinePojo stateMachinePojo;
    private final StateMachineConfig<String, String> config;

    StateMachineCompilerImpl(Set<Integer> requiredDevices, StateMachinePojo stateMachinePojo) {
        this.requiredDevices = requiredDevices;
        mapping = new TriggerTranslator();
        this.stateMachinePojo = stateMachinePojo;
        this.config = new StateMachineConfig<>();
    }

    @Override
    public StateMachine compileStateMachine(Map<Integer, DeviceCommandCompiler> commandCompilers) {
        final String initialState = findInitialState(stateMachinePojo);
        buildTriggers();
        buildStates(commandCompilers);

        final com.github.oxo42.stateless4j.StateMachine<String, String> stateMachine =
                new com.github.oxo42.stateless4j.StateMachine<>(initialState, config);

        return new StateMachineImpl(stateMachine, mapping);
    }

    private String findInitialState(StateMachinePojo stateMachinePojo) {
        return stateMachinePojo.getOptions().getInitialState();
    }

    private void buildTriggers() {
        stateMachinePojo.getTriggers().forEach(
                t -> mapping.addTrigger(t.getEventSourceId(), t.getEventPayload(), t.getName())
        );
    }

    private void buildStates(Map<Integer, DeviceCommandCompiler> commandCompilers) {
        stateMachinePojo.getStates().forEach(state -> buildSingleState(state, commandCompilers));
    }

    private void buildSingleState(StatePojo statePojo, Map<Integer, DeviceCommandCompiler> commandCompilers) {
        final String name = statePojo.getName();
        StateConfiguration<String, String> state = config.configure(name);
        addEntryCommandsToState(state, statePojo.getOnEntry(), commandCompilers);
        addExitCommandsToState(state, statePojo.getOnExit(), commandCompilers);
        addTransitions(state, statePojo.getTransitions());
    }

    private void addEntryCommandsToState(StateConfiguration<String, String> state,
            List<CommandPojo> entryCommands, Map<Integer, DeviceCommandCompiler> commandCompilers) {
        if (entryCommands == null || entryCommands.isEmpty()) {
            return;
        }

        for (CommandPojo cmd : entryCommands) {
            final DeviceCommand command = commandCompilers.get(cmd.getDeviceId()).compile(cmd.getCommandString());
            state.onEntry(command::execute);
        }
    }

    private void addExitCommandsToState(StateConfiguration<String, String> state,
            List<CommandPojo> exitCommands, Map<Integer, DeviceCommandCompiler> commandCompilers) {
        if (exitCommands == null || exitCommands.isEmpty()) {
            return;
        }

        for (CommandPojo cmd : exitCommands) {
            final DeviceCommand command = commandCompilers.get(cmd.getDeviceId()).compile(cmd.getCommandString());
            state.onExit(command::execute);
        }
    }

    private void addTransitions(StateConfiguration<String, String> state, List<TransitionPojo> transitions) {
        if (transitions == null) {
            return;
        }

        for (TransitionPojo t : transitions) {
            state.permit(t.getTriggerName(), t.getTargetState());
        }
    }
}
