package com.lexcorp.joura.runtime.handlers;

import com.lexcorp.joura.runtime.Trackable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Map;

public class Log4JEventHandler implements EventHandler {
    private static final Logger logger = LogManager.getLogger(Log4JEventHandler.class);

    private static final Marker OBJECT_MARKER = MarkerManager.getMarker("Object");
    private static final Marker METHOD_MARKER = MarkerManager.getMarker("Method").setParents(OBJECT_MARKER);
    private static final Marker FIELDS_MARKER = MarkerManager.getMarker("Fields").setParents(METHOD_MARKER);

    public Log4JEventHandler() {
    }

    @Override
    public <T extends Trackable> void accept(Long objId, T trackable, String methodName, Map<String, Object> fields) {
        String identifier = trackable.getTag();
        logger.debug(OBJECT_MARKER, String.format("[%s:%d] - Trackable object: %s", identifier, objId, trackable));
        logger.debug(METHOD_MARKER, String.format("[%s:%d] - Invoked method: %s", identifier, objId, methodName));
        logger.debug(FIELDS_MARKER, String.format("[%s:%d] - Editable fields: %s", identifier, objId, fields.toString()));
    }
}
