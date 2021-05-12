package com.lexcorp.joura.handlers;

import java.util.function.Function;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.handlers.EventHandler;
import com.lexcorp.joura.runtime.listeners.Entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntitiesTestHandler implements EventHandler {
    private int entitiesCount = 0;
    private int lastCheckEntitiesCount = 0;
    private Entity lastEntity;

    public EntitiesTestHandler() {
    }

    @Override
    public <T extends Trackable> void accept(Entity entity) {
        lastEntity = entity;
        entitiesCount++;
    }

    public void checkLastEntityMethodName(String expectedMethodName) {
        checkLastEntity(
                (entity -> expectedMethodName.equals(entity.getMethodName())),
                "Expected: " + expectedMethodName + " but actual " + lastEntity.getMethodName()
        );
    }

    public void checkLastEntity(Function<Entity, Boolean> consumer) {
        assertTrue(consumer.apply(lastEntity));
    }

    public void checkLastEntity(Function<Entity, Boolean> consumer, String errorMessage) {
        assertTrue(consumer.apply(lastEntity), errorMessage);
    }

    public void checkLastEntityReceived() {
        assertNotNull(lastEntity);
        checkEntityReceivedCount(1);
    }

    public void checkEntityReceivedCount(int count) {
        assertEquals(entitiesCount - lastCheckEntitiesCount, count);
        lastCheckEntitiesCount = entitiesCount;
    }
}
