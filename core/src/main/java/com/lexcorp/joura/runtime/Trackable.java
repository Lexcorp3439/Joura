package com.lexcorp.joura.runtime;

public interface Trackable {

    default void startTrack() {}

    default void stopTrack() {}

}
