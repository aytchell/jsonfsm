package com.github.aytchell.jsonfsm.input.pojos;

import lombok.Data;

@Data
public class TriggerPojo {
    private String name;
    private Integer eventSourceId;
    private String eventPayload;
}
