package com.lexcorp.joura.logger;

public class Markers {
    public static class Compile {
        public static final Marker PROCESSOR_MARKER = Marker.getMarker("PROCESSOR");
        public static final Marker STATISTIC = Marker.getMarker("STATISTIC");
        public static final Marker ALIAS_MARKER = Marker.getMarker("PROCESSOR").setParents(PROCESSOR_MARKER);

        public static final Marker START_PROCESS_MARKER = Marker.getMarker("START");
        public static final Marker END_PROCESS_MARKER = Marker.getMarker("END");

        public static final Marker START_METHOD_PROCESSING_MARKER = Marker.getMarker("START_METHOD_PROCESSING");
        public static final Marker END_METHOD_PROCESSING_MARKER = Marker.getMarker("END_METHOD_PROCESSING");

        public static final Marker START_ALIAS_ANALYSIS_MARKER = Marker.getMarker("START_ALIAS_ANALYSIS");
        public static final Marker END_ALIAS_ANALYSIS_MARKER = Marker.getMarker("END_ALIAS_ANALYSIS");

        public static final Marker EXPECTED_FIELDS_MARKER = Marker.getMarker("EXPECTED_FIELDS")
                .setParents(PROCESSOR_MARKER);
        public static final Marker RECEIVED_FIELDS_MARKER = Marker.getMarker("RECEIVED_FIELDS")
                .setParents(PROCESSOR_MARKER);
        public static final Marker START_ALIAS_METHOD_MARKER = Marker.getMarker("START_ALIAS_METHOD")
                .setParents(ALIAS_MARKER);
        public static final Marker ITER_ALIAS_METHOD_MARKER_START = Marker.getMarker("ITER_ALIAS_METHOD_START")
                .setParents(ALIAS_MARKER);
        public static final Marker ITER_ALIAS_METHOD_MARKER_END = Marker.getMarker("ITER_ALIAS_METHOD_END")
                .setParents(ALIAS_MARKER);
        public static final Marker END_ALIAS_METHOD_MARKER = Marker.getMarker("END_ALIAS_METHOD")
                .setParents(ALIAS_MARKER);
        public static final Marker CREATE_LOCAL_VAR_ANALYSIS = Marker.getMarker("CREATE_LOCAL_VAR_ANALYSIS")
                .setParents(ALIAS_MARKER);
        public static final Marker CHECK_VALID_ASSIGMENT = Marker.getMarker("CHECK_VALID_ASSIGMENT")
                .setParents(ALIAS_MARKER);
    }

    public static class Runtime {
        public static final Marker OBJECT_MARKER = Marker.getMarker("Object");
        public static final Marker METHOD_MARKER = Marker.getMarker("Method").setParents(OBJECT_MARKER);
        public static final Marker FIELDS_MARKER = Marker.getMarker("Fields").setParents(METHOD_MARKER);
    }
}
