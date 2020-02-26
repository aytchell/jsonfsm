package com.github.aytchell.feedbackstates.input.pojos;

import lombok.Data;

import java.util.List;

@Data
public class StateMachinePojo {
    private OptionsPojo options;
    private List<TriggerPojo> triggers;
    private List<StatePojo> states;
}
