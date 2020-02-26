package com.github.aytchell.feedbackstates.input.pojos;

import lombok.Data;

@Data
public class CommandPojo {
    private Integer deviceId;
    private String commandString;
}
