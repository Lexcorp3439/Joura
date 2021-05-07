package com.lexcorp.joura;

import com.lexcorp.joura.objects.SimpleTestObject;
import com.lexcorp.joura.runtime.handlers.Log4JEventHandler;
import com.lexcorp.joura.runtime.listeners.FieldChangeListener;

public class Main {
    public static void main(String[] args) {
        FieldChangeListener listener = FieldChangeListener.getInstance();
        listener.addEventHandler(new Log4JEventHandler());

        SimpleTestObject object = new SimpleTestObject();
        object.startTrack();
        object.testAssignAnnotation();
        object.stopTrack();
        System.out.println(object.toString());
    }
}
