package com.github.aytchell.feedbackstates.compiler;

import java.util.HashMap;
import java.util.Map;

public class TriggerTranslator {
    private final Map<String, String> mapping = new HashMap<>();

    public void addTrigger(int eventSourceId, String eventPayload, String triggerName) {
        final String key = computeKey(eventSourceId, eventPayload);
        mapping.put(key, triggerName);
    }

    public String getTriggerName(int eventSourceId, String eventPayload) {
        final String key = computeKey(eventSourceId, eventPayload);
        return mapping.get(key);
    }

    private String computeKey(int eventSourceId, String eventPayload) {
        return Integer.toString(eventSourceId) + "_" + eventPayload;
    }
}
