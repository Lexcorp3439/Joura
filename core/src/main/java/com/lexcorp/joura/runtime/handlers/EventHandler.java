package com.lexcorp.joura.runtime.handlers;

import java.util.Map;

import com.lexcorp.joura.runtime.Trackable;

@FunctionalInterface
public interface EventHandler {
    <T extends Trackable> void accept(T trackable, String methodName, Map<String, Object> fields);
}
