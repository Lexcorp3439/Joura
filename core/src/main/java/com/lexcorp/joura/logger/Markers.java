package com.lexcorp.joura.logger;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class Markers {
    public static class Compile {
        public static final Marker PROCESSOR_MARKER = MarkerManager.getMarker("PROCESSOR");

        public static final Marker EXPECTED_FIELDS_MARKER = MarkerManager.getMarker("EXPECTED_FIELDS")
                .setParents(PROCESSOR_MARKER);
        public static final Marker RECEIVED_FIELDS_MARKER = MarkerManager.getMarker("RECEIVED_FIELDS")
                .setParents(PROCESSOR_MARKER);
    }

    public static class Runtime {
        public static final Marker OBJECT_MARKER = MarkerManager.getMarker("Object");
        public static final Marker METHOD_MARKER = MarkerManager.getMarker("Method").setParents(OBJECT_MARKER);
        public static final Marker FIELDS_MARKER = MarkerManager.getMarker("Fields").setParents(METHOD_MARKER);
    }
}
