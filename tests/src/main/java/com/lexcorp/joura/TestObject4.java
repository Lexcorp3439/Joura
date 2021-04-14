package com.lexcorp.joura;

import static com.lexcorp.joura.StaticMethods.method;

public class TestObject4 implements Trackable {
    public int value1213;

    public void referenceMethod(TestObject4 trackObj) {
        TestObject4 ref1 = this;
        TestObject4 ref2 = ref1;
        TestObject4 obj = new TestObject4();
        trackObj.value1213 = 1000;
//        trackObj = this;
        obj.value1213 = 213134;

        ref2 = trackObj;
//        ref2.value1213 = 123;
//        obj = this;
        ref1 = new TestObject4();
        method(ref1.value1213, obj.value1213, ref2.value1213);
//        obj.value1213 = 123123;
    }
}
