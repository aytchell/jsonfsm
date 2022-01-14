package com.github.aytchell.jsonfsm.compiler;

import com.github.aytchell.jsonfsm.DeviceCommand;
import com.github.aytchell.jsonfsm.DeviceCommandCompiler;
import com.github.aytchell.jsonfsm.StateMachine;
import com.github.aytchell.jsonfsm.StateMachineCompiler;
import com.github.aytchell.jsonfsm.CompilationException;
import com.github.aytchell.jsonfsm.input.pojos.BehaviorPojo;
import com.github.aytchell.jsonfsm.input.pojos.StateMachinePojo;
import com.github.aytchell.jsonfsm.input.pojos.StatePojo;
import com.github.aytchell.jsonfsm.input.pojos.TransitionPojo;
import com.github.oxo42.stateless4j.StateConfiguration;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.Action;
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
            buildSingleState(state, commandCompilers);
        }
    }

    private void buildSingleState(StatePojo statePojo, Map<Integer, DeviceCommandCompiler> commandCompilers)
            throws CompilationException {
        final String name = statePojo.getName();
        StateConfiguration<String, String> state = config.configure(name);
        addEntryCommandsToState(state, name, statePojo.getOnEntry(), commandCompilers);
        addExitCommandsToState(state, name, statePojo.getOnExit(), commandCompilers);
        addTransitions(state, name, statePojo.getTransitions(), commandCompilers);
    }

    private void addEntryCommandsToState(
            StateConfiguration<String, String> state, String stateName,
            List<BehaviorPojo> entryCommands, Map<Integer, DeviceCommandCompiler> commandCompilers)
            throws CompilationException {
        final String location = "onEntry (" + stateName + ")";
        addBehaviorToState(location, entryCommands, commandCompilers, state::onEntry);
    }

    private void addExitCommandsToState(
            StateConfiguration<String, String> state, String stateName,
            List<BehaviorPojo> exitCommands, Map<Integer, DeviceCommandCompiler> commandCompilers)
            throws CompilationException {
        final String location = "onExit (" + stateName + ")";
        addBehaviorToState(location, exitCommands, commandCompilers, state::onExit);
    }

    private void addBehaviorToState(
            String location, List<BehaviorPojo> commands,
            Map<Integer, DeviceCommandCompiler> commandCompilers, ActionAppender appender)
            throws InternalCompilationException {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        for (BehaviorPojo cmd : commands) {
            try {
                final DeviceCommand command = new DeviceCommandWrapper(
                        commandCompilers.get(cmd.getDeviceId()).compile(cmd.getCommandString()),
                        location, cmd.getDeviceId(), cmd.getCommandString());
                appender.addBehavior(command::execute);
            } catch (Exception e) {
                throw new InternalCompilationException(e.getMessage(), e.getCause(),
                        location, cmd.getDeviceId(), cmd.getCommandString());
            }
        }
    }

    private void addTransitions(
            StateConfiguration<String, String> state, String stateName,
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
                    final String location =
                            "transition (" + stateName + " -> " + t.getTargetState() +
                                    "; trigger: " + t.getTriggerName() + ")";
                    final DeviceCommand command = compileDeviceCommandChain(
                            location, effects, commandCompilers);
                    state.permit(t.getTriggerName(), t.getTargetState(), command::execute);
                }
            } else {
                state.ignore(t.getTriggerName());
            }
        }
    }

    private DeviceCommand compileDeviceCommandChain(
            String location, List<BehaviorPojo> effects,
            Map<Integer,DeviceCommandCompiler> commandCompilers) throws CompilationException {
        final List<DeviceCommand> commands = new LinkedList<>();
        for (BehaviorPojo e : effects) {
            try {
                commands.add(new DeviceCommandWrapper(
                        commandCompilers.get(e.getDeviceId()).compile(e.getCommandString()),
                        location, e.getDeviceId(), e.getCommandString()));
            } catch (Exception exception) {
                // enrich exception with the information we used to compile the command
                throw new InternalCompilationException(exception.getMessage(), exception.getCause(),
                        location, e.getDeviceId(), e.getCommandString());
            }
        }
        return () -> commands.forEach(DeviceCommand::execute);
    }

    private interface ActionAppender {
        void addBehavior(final Action entryAction);
    }
}
