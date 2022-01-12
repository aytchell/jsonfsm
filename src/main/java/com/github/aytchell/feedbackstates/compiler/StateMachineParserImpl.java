package com.github.aytchell.feedbackstates.compiler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.aytchell.feedbackstates.StateMachineCompiler;
import com.github.aytchell.feedbackstates.input.pojos.BehaviorPojo;
import com.github.aytchell.feedbackstates.input.pojos.StateMachinePojo;
import com.github.aytchell.feedbackstates.input.pojos.StatePojo;
import com.github.aytchell.validator.Validator;
import com.github.aytchell.validator.exceptions.ValidationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StateMachineParserImpl {
    private final ObjectMapper mapper = new ObjectMapper();

    public StateMachineCompiler parseAndListRequiredDeviceIds(String jsonDescription) throws ValidationException {
        Validator.expect(jsonDescription, "jsonStateMachine").notNull().notBlank();

        final StateMachinePojo stateMachinePojo = parseJsonToPojo(jsonDescription);
        StateMachinePojoValidator.validate(stateMachinePojo);
        final Set<Integer> devices = extractRequiredDeviceIds(stateMachinePojo);
        final Set<Integer> eventSources = extractAcceptedEventSources(stateMachinePojo);

        return new StateMachineCompilerImpl(devices, eventSources, stateMachinePojo);
    }

    private StateMachinePojo parseJsonToPojo(String jsonDescription) throws ValidationException {
        try {
            return mapper.readValue(jsonDescription, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new ValidationException("Error while parsing given json: " + e.getMessage());
        }
    }

    private Set<Integer> extractRequiredDeviceIds(StateMachinePojo stateMachine) {
        Set<Integer> ids = new HashSet<>();
        stateMachine.getStates().stream()
                .map(this::extractRequiredDeviceIds)
                .forEach(ids::addAll);
        return ids;
    }

    private Set<Integer> extractAcceptedEventSources(StateMachinePojo stateMachine) {
        Set<Integer> ids = new HashSet<>();
        stateMachine.getTriggers().forEach(t -> ids.add(t.getEventSourceId()));
        return ids;
    }

    private Set<Integer> extractRequiredDeviceIds(StatePojo state) {
        final Set<Integer> ids = new HashSet<>();
        ids.addAll(extractRequiredEntryDeviceIds(state));
        ids.addAll(extractRequiredExitDeviceIds(state));
        return ids;
    }

    private Set<Integer> extractRequiredEntryDeviceIds(StatePojo state) {
        List<BehaviorPojo> entries = state.getOnEntry();
        if (entries == null || entries.isEmpty()) {
            return Set.of();
        }

        return entries.stream().map(BehaviorPojo::getDeviceId).collect(Collectors.toSet());
    }

    private Set<Integer> extractRequiredExitDeviceIds(StatePojo state) {
        List<BehaviorPojo> exits = state.getOnExit();
        if (exits == null || exits.isEmpty()) {
            return Set.of();
        }

        return exits.stream().map(BehaviorPojo::getDeviceId).collect(Collectors.toSet());
    }
}
