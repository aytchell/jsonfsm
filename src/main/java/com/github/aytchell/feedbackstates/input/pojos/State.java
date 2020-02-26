package com.github.aytchell.feedbackstates.input.pojos;

import lombok.Data;

import java.util.List;

@Data
public class State {
    private String name;
    private List<Command> onEntry;
    private List<Command> onExit;
    private List<Transition> transitions;
}
