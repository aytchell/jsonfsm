package com.github.aytchell.feedbackstates.input.pojos;

import lombok.Data;

@Data
public class Transition {
    private String triggerName;
    private String targetState;
}
