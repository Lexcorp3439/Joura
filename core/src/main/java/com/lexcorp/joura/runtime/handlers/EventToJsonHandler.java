package com.lexcorp.joura.runtime.handlers;

import com.lexcorp.joura.runtime.Trackable;

import java.util.Map;

public class EventToJsonHandler  implements  EventHandler {
    @Override
    public <T extends Trackable> void accept(Long objId, T trackable, String methodName, Map<String, Object> fields) {

    }
}
