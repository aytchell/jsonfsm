package com.github.aytchell.feedbackstates.compiler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.StateMachineParser;
import com.github.aytchell.feedbackstates.exceptions.MalformedInputException;
import com.github.aytchell.feedbackstates.input.pojos.CommandPojo;
import com.github.aytchell.feedbackstates.input.pojos.StateMachinePojo;
import com.github.aytchell.feedbackstates.input.pojos.StatePojo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StateMachineParserImpl implements StateMachineParser {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public StateMachineCompiler parseAndListRequiredDeviceIds(String jsonDescription) throws MalformedInputException {
        if (jsonDescription == null || jsonDescription.isEmpty()) {
            throw new MalformedInputException("StateMachine description is empty");
        }

        final StateMachinePojo stateMachinePojo = parseJsonToPojo(jsonDescription);
        validateParsedStateMachine(stateMachinePojo);
        final Set<Integer> devices = extractRequiredDeviceIds(stateMachinePojo);

        return new StateMachineCompilerImpl(devices, stateMachinePojo);
    }

    private StateMachinePojo parseJsonToPojo(String jsonDescription) throws MalformedInputException {
        try {
            return mapper.readValue(jsonDescription, new TypeReference<StateMachinePojo>() {
            });
        } catch (JsonProcessingException e) {
            throw new MalformedInputException("Error while parsing given json");
        }
    }

    private void validateParsedStateMachine(StateMachinePojo stateMachinePojo) throws MalformedInputException {
        if (stateMachinePojo.getStates() == null || stateMachinePojo.getStates().isEmpty()) {
            throw new MalformedInputException("StateMachine contains no states");
        }
    }

    private Set<Integer> extractRequiredDeviceIds(StateMachinePojo stateMachine) {
        Set<Integer> ids = new HashSet<>();
        stateMachine.getStates().stream()
                .map(this::extractRequiredDeviceIds)
                .forEach(ids::addAll);
        return ids;
    }

    private Set<Integer> extractRequiredDeviceIds(StatePojo state) {
        final Set<Integer> ids = new HashSet<>();
        ids.addAll(extractRequiredEntryDeviceIds(state));
        ids.addAll(extractRequiredExitDeviceIds(state));
        return ids;
    }

    private Set<Integer> extractRequiredEntryDeviceIds(StatePojo state) {
        List<CommandPojo> entries = state.getOnEntry();
        if (entries == null) {
            return Set.of();
        }

        return entries.stream().map(CommandPojo::getDeviceId).collect(Collectors.toSet());
    }

    private Set<Integer> extractRequiredExitDeviceIds(StatePojo state) {
        List<CommandPojo> exits = state.getOnExit();
        if (exits == null) {
            return Set.of();
        }

        return exits.stream().map(CommandPojo::getDeviceId).collect(Collectors.toSet());
    }
}
