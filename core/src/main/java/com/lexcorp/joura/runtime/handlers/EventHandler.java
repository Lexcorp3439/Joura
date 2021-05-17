package com.lexcorp.joura.runtime.handlers;


import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.listeners.Event;

@FunctionalInterface
public interface EventHandler {
    <T extends Trackable> void accept(Event event);
}
