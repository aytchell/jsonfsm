package com.github.aytchell.feedbackstates;

public interface EventInput {
    Integer getEventSourceId();
    String getEventPayload();
}
