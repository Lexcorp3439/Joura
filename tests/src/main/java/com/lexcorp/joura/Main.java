package com.lexcorp.joura;

public class Main {
    public static void main(String[] args) {
        TestObject1 object = new TestObject1();
        object.startTrack();
        object.stopTrack();
        object.update();
        System.out.println(object.toString());
    }
}
