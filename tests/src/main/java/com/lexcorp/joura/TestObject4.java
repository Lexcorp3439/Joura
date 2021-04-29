package com.lexcorp.joura;

import static com.lexcorp.joura.StaticMethods.method;

public class TestObject4 implements Trackable {
    public int value1213;
    public TestObject4 testObject4;

    public void referenceMethod(TestObject4 trackObj) {
        TestObject4 ref1 = this;
        TestObject4 ref2 = ref1;
        TestObject4 obj = new TestObject4();
        trackObj.value1213 = 1000;
        obj.value1213 = 213134;
        ref1.value1213 = 12343124;

        ref2 = trackObj;
        ref1 = new TestObject4();
        method(ref1.value1213, obj.value1213, ref2.value1213);

        if (true) {
            ref1.testObject4 = new TestObject4();
            ref1.testObject4.testObject4 = this;
        }
    }
}
