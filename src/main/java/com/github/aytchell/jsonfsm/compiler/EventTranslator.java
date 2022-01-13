package com.github.aytchell.jsonfsm.compiler;

import java.util.HashMap;
import java.util.Map;

class EventTranslator {
    private final Map<String, String> mapping = new HashMap<>();

    public void addEvent(int eventSourceId, String eventPayload, String eventName) {
        final String key = computeKey(eventSourceId, eventPayload);
        mapping.put(key, eventName);
    }

    public String getEventName(int eventSourceId, String eventPayload) {
        final String key = computeKey(eventSourceId, eventPayload);
        return mapping.get(key);
    }

    private String computeKey(int eventSourceId, String eventPayload) {
        return eventSourceId + "_" + eventPayload;
    }
}
