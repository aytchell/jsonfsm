package com.github.aytchell.feedbackstates.input.pojos;

import lombok.Data;

import java.util.List;

@Data
public class StateMachine {
    private Options options;
    private List<Trigger> trigger;
    private List<State> states;
}
