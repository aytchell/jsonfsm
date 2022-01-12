package com.github.aytchell.feedbackstates.input.pojos;

import lombok.Data;

import java.util.List;

@Data
public class StatePojo {
    private String name;
    private List<BehaviorPojo> onEntry;
    private List<BehaviorPojo> onExit;
    private List<TransitionPojo> transitions;
}
