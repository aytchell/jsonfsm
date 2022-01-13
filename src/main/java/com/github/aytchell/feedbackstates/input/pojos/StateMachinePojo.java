package com.github.aytchell.feedbackstates.input.pojos;

import lombok.Data;

import java.util.List;

@Data
public class StateMachinePojo {
    private String initialState;
    private List<TriggerPojo> triggers;
    private List<StatePojo> states;
    private List<String> finalStates;
}
