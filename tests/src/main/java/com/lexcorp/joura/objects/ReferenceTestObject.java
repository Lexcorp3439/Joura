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

    @ExpectedFields(fields = {"value2", "reference"})
    public void testWriteFieldsWithReferenceSimple() {
        this.reference = new ReferenceTestObject();
        this.reference.reference = this;
        this.reference.reference.value2 = 100;
    }

    @ExpectedFields(fields = {"value1", "value2", "value3", "reference"})
    public void testWriteFieldsWithReferenceCore() {
        this.reference = new ReferenceTestObject();
        this.reference.reference = this;
        ReferenceTestObject a = this.reference.reference;
        a.reference = new ReferenceTestObject();
        ReferenceTestObject b = this.reference;
        ReferenceTestObject c = b.reference;
        b.value1 = 1000;
        c.value2 = 1000;
        this.reference.reference.value2 = 100;
        a.value3 = 100;
    }

    @ExpectedFields(fields = {"value1"})
    public void testWriteFieldsWithReference() {
        ReferenceTestObject a, b, c, d, e, f;
        d = new ReferenceTestObject(); // {d} = {NEW}
        a = this; // {a} = {THIS}
        b = a; // b -> {a} = {THIS}
        c = b; // c -> {b} = {THIS}
        c = new ReferenceTestObject(); // {c} = {THIS, NEW}
        a = d; // {a} = {THIS, NEW}
        e = new ReferenceTestObject(); // {e} = {NEW}
        c = d; // c -> {d} = {NEW} ; {c} = {THIS, NEW}
        f = this; // {f} = {THIS}
        e = f; // e -> {f} = {THIS} ; {e} = {THIS, NEW}
        b = e; // b -> {e} = {THIS, NEW} ; {b} = {THIS, NEW}
        d = c; // d -> {c} = {THIS, NEW} ; {d} = {THIS, NEW}
        e.value1 = 100;
    }

    @ExpectedFields(fields = {"reference", "value1", "value3"})
    public void testWriteFieldsWithReferenceSmall() {
        ReferenceTestObject a, b, c;
        a = this;
        a.reference = this;
        b = new ReferenceTestObject();
        c = a.reference;
        a.value1 = 100; // {a} = {THIS, NEW}
        b.value2 = 100;
        c.value3 = 100;
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
