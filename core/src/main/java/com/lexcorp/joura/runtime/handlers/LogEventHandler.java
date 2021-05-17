package com.lexcorp.joura.runtime.handlers;

import com.lexcorp.joura.logger.JouraLogger;
import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.listeners.Entity;

import static com.lexcorp.joura.logger.Markers.Runtime.FIELDS_MARKER;
import static com.lexcorp.joura.logger.Markers.Runtime.METHOD_MARKER;
import static com.lexcorp.joura.logger.Markers.Runtime.OBJECT_MARKER;

public class LogEventHandler implements EventHandler {
    private static final JouraLogger logger = JouraLogger.get(LogEventHandler.class);

    public LogEventHandler() {
    }

    @Override
    public <T extends Trackable> void accept(Entity entity) {
        logger.info(OBJECT_MARKER, String.format(
                "[%s:%d] - Trackable object: %s",
                entity.getTag(), entity.getId(), entity.getTrackable()
        ));
        logger.info(METHOD_MARKER, String.format(
                "[%s:%d] - Invoked method: %s",
                entity.getTag(), entity.getId(), entity.getMethodName()
        ));
        logger.info(FIELDS_MARKER, String.format(
                "[%s:%d] - Editable fields: %s",
                entity.getTag(), entity.getId(), entity.getFields().toString()
        ));
    }
}
