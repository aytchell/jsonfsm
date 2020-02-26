package com.github.aytchell.feedbackstates.input.pojos;

import lombok.Data;

@Data
public class TriggerPojo {
    private String name;
    private Integer eventSourceId;
    private String eventPayload;
}
