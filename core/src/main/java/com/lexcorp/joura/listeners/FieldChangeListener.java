package com.lexcorp.joura.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.lexcorp.joura.Trackable;
import com.lexcorp.joura.handlers.EventHandler;

public class FieldChangeListener {
    private final Logger log = Logger.getLogger(FieldChangeListener.class.getName());
    private static final FieldChangeListener INSTANCE = new FieldChangeListener();
    private final List<EventHandler> eventHandlers = new ArrayList<>();
    private final Map<Trackable, List<EventHandler>> instanceEventHandlers = new HashMap<>();
    private final Map<Class<? extends Trackable>, List<EventHandler>> classEventHandlers = new HashMap<>();

    public static FieldChangeListener getInstance() {
        return INSTANCE;
    }

    private FieldChangeListener() {
    }

    public <T extends Trackable> void accept(T trackable, String methodName, Map<String, Object> fields) {
        log.info(methodName);
        log.info(fields.toString());
        log.info(trackable.getClass().getName());
        eventHandlers.forEach(handler -> handler.accept(trackable, methodName, fields));
        if (instanceEventHandlers.containsKey(trackable)) {
            instanceEventHandlers.get(trackable).forEach(handler -> handler.accept(trackable, methodName, fields));
        }
        if (classEventHandlers.containsKey(trackable.getClass())) {
            classEventHandlers.get(trackable.getClass()).forEach(handler -> handler.accept(trackable, methodName, fields));
        }
    }

    public <T extends Trackable> void addEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public <T extends Trackable> void addInstanceEventHandler(T trackable, EventHandler eventHandler) {
        if (!instanceEventHandlers.containsKey(trackable)) {
            instanceEventHandlers.put(trackable, new ArrayList<>());
        }
        instanceEventHandlers.get(trackable).add(eventHandler);
    }

    public <T extends Trackable> void addClassEventHandler(Class<T> trackableClass, EventHandler eventHandler) {
        if (!classEventHandlers.containsKey(trackableClass)) {
            classEventHandlers.put(trackableClass, new ArrayList<>());
        }
        classEventHandlers.get(trackableClass).add(eventHandler);
    }

}
