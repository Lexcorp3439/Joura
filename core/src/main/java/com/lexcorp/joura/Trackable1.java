package com.lexcorp.joura;

public interface Trackable1 {

    default void startTrack() {}

    default void stopTrack() {}

    default void getSlice() {
        this.getClass();
    }
}
