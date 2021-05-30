package com.lexcorp.joura.runtime.handlers;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.listeners.Event;

public class EventsCountEventHandler implements EventHandler {
    private Long eventsCount = 0L;

    @Override
    public <T extends Trackable> void accept(Event event) {
        eventsCount++;
    }

    public void print() {
        System.out.println("EVENTS COUNT = " + eventsCount);
        ;
    }
}
