package com.lexcorp.joura.runtime;

public interface Trackable {

    default void startTrack() {}

    default void stopTrack() {}

    default void setIdentifier(String newIdentifier) {}

    default String getIdentifier() {return "UNKNOWN";}
}
