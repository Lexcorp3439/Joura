package com.lexcorp.joura.objects;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.options.test.ExpectedFields;

public class ReferenceTestObject implements Trackable {
    private ReferenceTestObject reference;
    private int value1 = 0;
    private int value2 = 0;
    private int value3 = 0;


    @ExpectedFields(fields = {"value1"})
    public void testWriteFieldsWithField() {
        this.reference = this;
        this.reference.value1 = 100;
    }

    @ExpectedFields(fields = {"value2"})
    public void testWriteFieldsWithReferenceSimple() {
        this.reference = new ReferenceTestObject();
        this.reference.reference = this;
        this.reference.reference.value2 = 100;
    }

    @ExpectedFields(fields = {"value2"})
    public void testWriteFieldsWithReferenceAfterMethodCall() {
        testWriteFieldsWithReferenceSimple();
        this.reference.reference.value2 = 1000;
    }

    @ExpectedFields(fields = {"value1"})
    public void testWithArgs(ReferenceTestObject reference) {
        reference.value1 = 100;
    }

    @ExpectedFields(fields = {"value3"})
    public void testWithHardArgs(ReferenceTestObject reference) {
        reference.reference.value3 = 100;
    }
}
