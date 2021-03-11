package com.lexcorp.joura;

public class Main {
    public static void main(String[] args) {
        TestObject object = new TestObject();
        object.startTrack();
        object.stopTrack();
        object.update();
        System.out.println(object.toString());
    }
}
