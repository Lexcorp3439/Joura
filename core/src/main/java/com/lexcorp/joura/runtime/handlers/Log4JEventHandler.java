package com.lexcorp.joura.runtime.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.listeners.Entity;

import static com.lexcorp.joura.logger.Markers.Runtime.FIELDS_MARKER;
import static com.lexcorp.joura.logger.Markers.Runtime.METHOD_MARKER;
import static com.lexcorp.joura.logger.Markers.Runtime.OBJECT_MARKER;

public class Log4JEventHandler implements EventHandler {
    private static final Logger logger = LogManager.getLogger(Log4JEventHandler.class);

    public Log4JEventHandler() {
    }

    @Override
    public <T extends Trackable> void accept(Entity entity) {
        logger.debug(OBJECT_MARKER, String.format(
                "[%s:%d] - Trackable object: %s",
                entity.getTag(), entity.getId(), entity.getTrackable()
        ));
        logger.debug(METHOD_MARKER, String.format(
                "[%s:%d] - Invoked method: %s",
                entity.getTag(), entity.getId(), entity.getMethodName()
        ));
        logger.debug(FIELDS_MARKER, String.format(
                "[%s:%d] - Editable fields: %s",
                entity.getTag(), entity.getId(), entity.getFields().toString()
        ));
    }
}
