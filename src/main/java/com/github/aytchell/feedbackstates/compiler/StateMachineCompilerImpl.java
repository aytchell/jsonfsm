package com.github.aytchell.feedbackstates.compiler;

import com.github.aytchell.feedbackstates.DeviceCommand;
import com.github.aytchell.feedbackstates.DeviceCommandCompiler;
import com.github.aytchell.feedbackstates.StateMachine;
import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.exceptions.CompilationException;
import com.github.aytchell.feedbackstates.input.pojos.BehaviorPojo;
import com.github.aytchell.feedbackstates.input.pojos.StateMachinePojo;
import com.github.aytchell.feedbackstates.input.pojos.StatePojo;
import com.github.aytchell.feedbackstates.input.pojos.TransitionPojo;
import com.github.oxo42.stateless4j.StateConfiguration;
import com.github.oxo42.stateless4j.StateMachineConfig;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

class StateMachineCompilerImpl implements StateMachineCompiler {
    @Getter
    private final Set<Integer> requiredDevices;
    @Getter
    private final Set<Integer> acceptedEventSources;

    private final EventTranslator mapping;
    private final StateMachinePojo stateMachinePojo;
    private final StateMachineConfig<String, String> config;

    StateMachineCompilerImpl(Set<Integer> requiredDevices, Set<Integer> acceptedEventSources,
            StateMachinePojo stateMachinePojo) {
        this.requiredDevices = requiredDevices;
        this.acceptedEventSources = acceptedEventSources;
        mapping = new EventTranslator();
        this.stateMachinePojo = stateMachinePojo;
        this.config = new StateMachineConfig<>();
    }

    @Override
    public StateMachine compileStateMachine(Map<Integer, DeviceCommandCompiler> commandCompilers)
            throws CompilationException {
        checkGivenCompilers(commandCompilers);
        final String initialState = findInitialState(stateMachinePojo);
        buildTriggers();
        buildStates(commandCompilers);

        final com.github.oxo42.stateless4j.StateMachine<String, String> stateMachine =
                new com.github.oxo42.stateless4j.StateMachine<>(initialState, config);

        return new StateMachineImpl(stateMachine, requiredDevices, acceptedEventSources, mapping);
    }

    private void checkGivenCompilers(Map<Integer, DeviceCommandCompiler> commandCompilers) throws CompilationException {
        for (Integer id : requiredDevices) {
            if (!commandCompilers.containsKey(id)) {
                throw new CompilationException("compiler for commands of device " + id + " is missing");
            }
        }
    }

    private String findInitialState(StateMachinePojo stateMachinePojo) {
        return stateMachinePojo.getInitialState();
    }

    private void buildTriggers() {
        stateMachinePojo.getTriggers().forEach(
                t -> mapping.addEvent(t.getEventSourceId(), t.getEventPayload(), t.getName())
        );
    }

    private void buildStates(Map<Integer, DeviceCommandCompiler> commandCompilers) throws CompilationException {
        for (StatePojo state : stateMachinePojo.getStates()) {
            try {
                // inside this method we might call one or more device command compilers and
                // nobody knows what might happen ...
                buildSingleState(state, commandCompilers);
            } catch (Exception e) {
                throw new CompilationException(
                        "Exception while compiling state '" + state.getName() + "': " + e.getMessage(), e);
            }
        }
    }

    private void buildSingleState(StatePojo statePojo, Map<Integer, DeviceCommandCompiler> commandCompilers)
            throws CompilationException {
        final String name = statePojo.getName();
        StateConfiguration<String, String> state = config.configure(name);
        addEntryCommandsToState(state, statePojo.getOnEntry(), commandCompilers);
        addExitCommandsToState(state, statePojo.getOnExit(), commandCompilers);
        addTransitions(state, statePojo.getTransitions());
    }

    private void addEntryCommandsToState(StateConfiguration<String, String> state,
                                         List<BehaviorPojo> entryCommands, Map<Integer, DeviceCommandCompiler> commandCompilers)
            throws CompilationException {
        if (entryCommands == null || entryCommands.isEmpty()) {
            return;
        }

        for (BehaviorPojo cmd : entryCommands) {
            final DeviceCommand command = commandCompilers.get(cmd.getDeviceId()).compile(cmd.getCommandString());
            state.onEntry(command::execute);
        }
    }

    private void addExitCommandsToState(StateConfiguration<String, String> state,
                                        List<BehaviorPojo> exitCommands, Map<Integer, DeviceCommandCompiler> commandCompilers)
            throws CompilationException {
        if (exitCommands == null || exitCommands.isEmpty()) {
            return;
        }

        for (BehaviorPojo cmd : exitCommands) {
            final DeviceCommand command = commandCompilers.get(cmd.getDeviceId()).compile(cmd.getCommandString());
            state.onExit(command::execute);
        }
    }

    private void addTransitions(StateConfiguration<String, String> state, List<TransitionPojo> transitions) {
        if (transitions == null) {
            return;
        }

        for (TransitionPojo t : transitions) {
            final Boolean ignore = t.getIgnore();
            if (ignore == null || !ignore) {
                state.permit(t.getTriggerName(), t.getTargetState());
            } else {
                state.ignore(t.getTriggerName());
            }
        }
    }
}
