package com.lexcorp.joura.objects;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.options.test.ExpectedFields;
import com.lexcorp.joura.utils.Methods;

import static com.lexcorp.joura.utils.Methods.createStatic;

public class MethodInvocationTestObject implements Trackable {
    private ReferenceTestObject reference;
    private int value1 = 0;
    private int value2 = 0;
    private int value3 = 0;

    public ReferenceTestObject getReference() {
        return reference;
    }

    public MethodInvocationTestObject setReference(ReferenceTestObject reference) {
        this.reference = reference;
        return this;
    }

    public int getValue1() {
        return value1;
    }

    public MethodInvocationTestObject setValue1(int value1) {
        this.value1 = value1;
        return this;
    }

    public MethodInvocationTestObject() {
    }

    public MethodInvocationTestObject(int value1, int value2, int value3) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    @ExpectedFields(fields = {})
    public void testSomeInvocations() {
        MethodInvocationTestObject m1 = createStatic().setValue1(value2).setReference(new ReferenceTestObject());
        Methods methods = new Methods();
        MethodInvocationTestObject m2 = methods.create().setValue1(value3).call();
    }

    public MethodInvocationTestObject call() {
        return this;
    }

}
