package com.lexcorp.joura;

import com.lexcorp.joura.objects.SimpleTestObject;
import com.lexcorp.joura.runtime.handlers.LogEventHandler;
import com.lexcorp.joura.runtime.listeners.FieldChangeReceiver;

public class Main {
    public static void main(String[] args) {
        FieldChangeReceiver listener = FieldChangeReceiver.getInstance();
        listener.addEventHandler(new LogEventHandler());

        SimpleTestObject object = new SimpleTestObject();
        object.startTrack();
        object.testAssignAnnotation();
        object.stopTrack();
        System.out.println(object.toString());
    }
}
