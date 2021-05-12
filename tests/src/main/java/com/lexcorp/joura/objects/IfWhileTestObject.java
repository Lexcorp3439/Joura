package com.lexcorp.joura.objects;

import java.util.Random;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.options.test.ExpectedFields;

import static com.lexcorp.joura.utils.StaticMethods.method;

public class IfWhileTestObject implements Trackable {
    public int value1;
    public int value2;
    public int value3;
    public int value4;
    public int value5;
    public IfWhileTestObject testObject;

    private IfWhileTestObject foo() {
        return new IfWhileTestObject();
    }

    @ExpectedFields(fields = {"value1", "value3", "value5", "testObject"})
    public IfWhileTestObject referenceMethod(IfWhileTestObject trackObj) {
        IfWhileTestObject ref1 = this;
        IfWhileTestObject ref2 = ref1;
        IfWhileTestObject obj = new IfWhileTestObject();
        trackObj.value3 = 1000;
        obj.value1 = 213134;

        while (new Random().nextBoolean()) {
            ref1.value1 = 12343124;
            ref2 = trackObj;
            ref1 = new IfWhileTestObject();
            method(ref1.value1, obj.value1, ref2.value1);

            if (new Random().nextBoolean()) {
                ref1.testObject = new IfWhileTestObject();
                ref1.testObject.testObject = this;
            }

            ref1.testObject.testObject.testObject = null;
            ref1.testObject.testObject.value5 = 1000;
            ref1.testObject = foo();
            ref1.value2 = 100;
        }

        return new IfWhileTestObject();
    }
}
