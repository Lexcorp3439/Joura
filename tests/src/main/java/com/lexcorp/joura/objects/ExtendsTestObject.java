package com.lexcorp.joura.objects;

import com.lexcorp.joura.tests.ExpectedFields;

public class ExtendsTestObject extends SimpleTestObject {
    private int value1 = 0;
    private Integer value2 = 2;
    private boolean value3 = true;

    @ExpectedFields(fields = {"value1"})
    public void setValue1(int value1) {
        this.value1 = value1;
    }

    @Override
    @ExpectedFields(fields = {"value3"})
    public int testSimpleAssign() {
        value3 = false;
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
