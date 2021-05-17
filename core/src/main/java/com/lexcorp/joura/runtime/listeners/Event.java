package com.lexcorp.joura.runtime.listeners;

import java.util.Map;

import com.lexcorp.joura.runtime.Trackable;

public class Event {
    private Long id;
    private String tag;
    private Trackable trackable;
    private String methodName;
    private Map<String, Object> fields;

    public Event(Long id, String tag, Trackable trackable, String methodName, Map<String, Object> fields) {
        this.id = id;
        this.tag = tag;
        this.trackable = trackable;
        this.methodName = methodName;
        this.fields = fields;
    }

    public Long getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public Trackable getTrackable() {
        return trackable;
    }

    public String getMethodName() {
        return methodName;
    }

    public Map<String, Object> getFields() {
        return fields;
    }
}
