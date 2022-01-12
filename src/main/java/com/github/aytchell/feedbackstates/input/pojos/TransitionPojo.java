package com.github.aytchell.feedbackstates.input.pojos;

import lombok.Data;

import java.util.List;

@Data
public class TransitionPojo {
    private String triggerName;
    private String targetState;
    private List<BehaviorPojo> effects;
    private Boolean ignore;
}
