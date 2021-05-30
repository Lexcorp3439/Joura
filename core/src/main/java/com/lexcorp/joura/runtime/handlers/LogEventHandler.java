package com.lexcorp.joura.runtime.handlers;

import com.lexcorp.joura.logger.JouraLogger;
import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.listeners.Event;

import static com.lexcorp.joura.logger.Markers.Runtime.FIELDS_MARKER;
import static com.lexcorp.joura.logger.Markers.Runtime.METHOD_MARKER;
import static com.lexcorp.joura.logger.Markers.Runtime.OBJECT_MARKER;

public class LogEventHandler implements EventHandler {
    private static final JouraLogger logger = JouraLogger.get(LogEventHandler.class);

    public LogEventHandler() {
    }

    @Override
    public <T extends Trackable> void accept(Event event) {
        logger.debug(OBJECT_MARKER, String.format(
                "[%s:%d] - Trackable object: %s",
                event.getTag(), event.getId(), event.getTrackable()
        ));
        logger.debug(METHOD_MARKER, String.format(
                "[%s:%d] - Invoked method: %s",
                event.getTag(), event.getId(), event.getMethodName()
        ));
        logger.debug(FIELDS_MARKER, String.format(
                "[%s:%d] - Editable fields: %s",
                event.getTag(), event.getId(), event.getFields().toString()
        ));
    }
}
