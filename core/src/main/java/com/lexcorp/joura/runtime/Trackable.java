package com.lexcorp.joura.runtime;

public interface Trackable extends Cloneable {

    default void startTrack() {
    }

    default void stopTrack() {
    }

    default void setTag(String newIdentifier) {
    }

    default String getTag() {
        return "UNKNOWN";
    }
}
