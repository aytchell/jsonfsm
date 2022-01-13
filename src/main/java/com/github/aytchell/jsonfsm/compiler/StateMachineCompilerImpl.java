package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.DeviceCommand;
import com.github.aytchell.jsonfsm.DeviceCommandCompiler;
import com.github.aytchell.jsonfsm.StateMachine;
import com.github.aytchell.jsonfsm.StateMachineCompiler;
import com.github.aytchell.jsonfsm.exceptions.CompilationException;
import com.github.aytchell.jsonfsm.input.pojos.BehaviorPojo;
import com.github.aytchell.jsonfsm.input.pojos.StateMachinePojo;
import com.github.aytchell.jsonfsm.input.pojos.StatePojo;
import com.github.aytchell.jsonfsm.input.pojos.TransitionPojo;
import com.github.oxo42.stateless4j.StateConfiguration;
import com.github.oxo42.stateless4j.StateMachineConfig;
import lombok.Getter;

import java.util.*;

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
        final Set<String> finalStates = findFinalStates(stateMachinePojo);
        buildTriggers();
        buildStates(commandCompilers);

        final com.github.oxo42.stateless4j.StateMachine<String, String> stateMachine =
                new com.github.oxo42.stateless4j.StateMachine<>(initialState, config);

        return new StateMachineImpl(stateMachine, finalStates, requiredDevices, acceptedEventSources, mapping);
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

    private Set<String> findFinalStates(StateMachinePojo stateMachinePojo) {
        final List<String> finalState = stateMachinePojo.getFinalStates();
        if (finalState == null || finalState.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(finalState);
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
        addTransitions(state, statePojo.getTransitions(), commandCompilers);
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

    private void addTransitions(StateConfiguration<String, String> state,
                                List<TransitionPojo> transitions,
                                Map<Integer, DeviceCommandCompiler> commandCompilers) throws CompilationException {
        if (transitions == null) {
            return;
        }

        for (TransitionPojo t : transitions) {
            final Boolean ignore = t.getIgnore();
            if (ignore == null || !ignore) {
                List<BehaviorPojo> effects = t.getEffects();
                if (effects == null || effects.isEmpty()) {
                    state.permit(t.getTriggerName(), t.getTargetState());
                } else {
                    final DeviceCommand command = compileDeviceCommandChain(effects, commandCompilers);
                    state.permit(t.getTriggerName(), t.getTargetState(), command::execute);
                }
            } else {
                state.ignore(t.getTriggerName());
            }
        }
    }

    private DeviceCommand compileDeviceCommandChain(
            List<BehaviorPojo> effects,
            Map<Integer,DeviceCommandCompiler> commandCompilers) throws CompilationException {
        final List<DeviceCommand> commands = new LinkedList<>();
        for (BehaviorPojo e : effects) {
            commands.add(commandCompilers.get(e.getDeviceId()).compile(e.getCommandString()));
        }
        return () -> { commands.forEach(DeviceCommand::execute); };
    }
}
