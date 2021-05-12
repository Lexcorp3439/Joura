package com.lexcorp.joura.runtime.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.handlers.EventHandler;
import com.lexcorp.joura.runtime.handlers.Log4JEventHandler;

public class FieldChangeReceiver {
    //    private final Logger log = Logger.getLogger(FieldChangeListener.class.getName());
    private static final Logger log = LogManager.getLogger(Log4JEventHandler.class);
    private static final FieldChangeReceiver INSTANCE = new FieldChangeReceiver();
    private final List<EventHandler> eventHandlers = new ArrayList<>();
    private final Map<Trackable, List<EventHandler>> instanceEventHandlers = new HashMap<>();
    private final Map<Trackable, Long> instancesId = new WeakHashMap<>();
    private final Map<Class<? extends Trackable>, List<EventHandler>> classEventHandlers = new HashMap<>();

    public static FieldChangeReceiver getInstance() {
        return INSTANCE;
    }

    private FieldChangeReceiver() {
    }

    @SuppressWarnings("unused")
    public <T extends Trackable> void accept(T trackable, String methodName, Map<String, Object> fields) {
        Long id = Optional.ofNullable(instancesId.get(trackable)).orElse((long) instancesId.size());
        instancesId.put(trackable, (long) instancesId.size());
        Entity entity = new Entity(id, trackable.getTag(), trackable, methodName, fields);

        Consumer<EventHandler> consumer = handler -> handler.accept(entity);

        eventHandlers.forEach(consumer);
        if (instanceEventHandlers.containsKey(trackable)) {
            instanceEventHandlers.get(trackable).forEach(consumer);
        }
        if (classEventHandlers.containsKey(trackable.getClass())) {
            classEventHandlers.get(trackable.getClass()).forEach(consumer);
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
