package com.lexcorp.joura.objects;

import java.util.ArrayList;
import java.util.List;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.options.Assign;
import com.lexcorp.joura.runtime.options.test.ExpectedFields;


public class SimpleTestObject implements Trackable {
    private int value1 = 0;
    private Integer value2 = 2;
    private boolean value3 = true;
    private List<Integer> integers = new ArrayList<>();

    @ExpectedFields(fields = {})
    public int getValue1() {
        return value1;
    }

    @ExpectedFields(fields = {"value1"})
    public SimpleTestObject testAssignWithThis(Integer value1) {
        this.value1 = value1;
        System.out.println(this.value1);
        return this;
    }

    @ExpectedFields(fields = {"value1"})
    public int testSimpleAssign() {
        value1 = 1000;
        return value1;
    }

    @ExpectedFields(fields = {"value1"})
    public void testAssignWithMethod() {
        this.value1 = change();
    }

    @Assign(fields = {"value2", "value1"})
    @ExpectedFields(fields = {"value2", "value1"})
    public void testAssignAnnotation() {
        System.out.println();;
    }

    @ExpectedFields(fields = {"value2"})
    public void testSendFieldToArgs() {
        change(this.value2);
    }

    @ExpectedFields(fields = {"integers"})
    public void testChangeMutableField() {
        this.integers.add(100);
    }

    @ExpectedFields(fields = {})
    public void testWriteFieldsWithReference() {
        SimpleTestObject ref2 = this;
        ref2.value1 = 100;
    }
    
    @ExpectedFields(fields = {})
    public void testWriteFieldsWithOtherReference() {
        SimpleTestObject ref1 = new SimpleTestObject();
        ref1.value1 = 100;
    }
    
    public static List<Integer> change(Integer value) {
        value = 100;
        return null;
    }

    public static int change() {
        return 1000;
    }
    
    @Override
    public String toString() {
        return "com.lexcorp.joura.TestObject{" +
                "value1=" + value1 +
                ", value2=" + value2 +
                '}';
    }
}
