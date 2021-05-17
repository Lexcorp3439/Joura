package com.lexcorp.joura.handlers;

import java.util.function.Function;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.handlers.EventHandler;
import com.lexcorp.joura.runtime.listeners.Event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventsTestHandler implements EventHandler {
    private int entitiesCount = 0;
    private int lastCheckEntitiesCount = 0;
    private Event lastEvent;

    public EventsTestHandler() {
    }

    @Override
    public <T extends Trackable> void accept(Event event) {
        lastEvent = event;
        entitiesCount++;
    }

    public void checkLastEntityMethodName(String expectedMethodName) {
        checkLastEntity(
                (entity -> expectedMethodName.equals(entity.getMethodName())),
                "Expected: " + expectedMethodName + " but actual " + lastEvent.getMethodName()
        );
    }

    public void checkLastEntity(Function<Event, Boolean> consumer) {
        assertTrue(consumer.apply(lastEvent));
    }

    public void checkLastEntity(Function<Event, Boolean> consumer, String errorMessage) {
        assertTrue(consumer.apply(lastEvent), errorMessage);
    }

    public void checkLastEntityReceived() {
        assertNotNull(lastEvent);
        checkEntityReceivedCount(1);
    }

    public void checkEntityReceivedCount(int count) {
        assertEquals(entitiesCount - lastCheckEntitiesCount, count);
        lastCheckEntitiesCount = entitiesCount;
    }
}
