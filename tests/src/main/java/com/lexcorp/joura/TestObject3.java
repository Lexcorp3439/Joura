package com.lexcorp.joura;

public class TestObject3 extends TestObject2 implements Trackable {
    public int value1213;

    public TestObject3() {
        value1213 = 123;
    }

    public void referenceMethod() {
        TestObject3 ref2 = this;
        TestObject3 ref3 = ref2;
//        StaticMethods s = new StaticMethods();
//        s.value1213 = 1000;
        ref3.value1213 = 100;
    }


}
