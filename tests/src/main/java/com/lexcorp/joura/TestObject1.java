package com.lexcorp.joura;

import java.util.ArrayList;
import java.util.List;

import com.lexcorp.joura.options.Assign;
import com.lexcorp.joura.options.TrackInitializer;

import static com.lexcorp.joura.StaticMethods.method;


public class TestObject1 implements Trackable {
    private int value1 = 0;
    private Integer value2 = 2;
    private boolean value3 = true;
    private List<Integer> integers = new ArrayList<>();

    public int getValue1() {
        return value1;
    }

    public TestObject1 setValue1(int value1) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        this.value1 = value1;
        System.out.println("Hello");
        return this;
    }

    public void invokeMethod() {
        this.value3 = !this.value3;
        method(value2, value1, integers);
    }

    public void referenceMethod() {
        Trackable ref1 = this;
        TestObject1 ref2 = this;
        ref2.value1 = 100;
        method(value2, value1, integers);
        method(this.value2, this.value1, this.integers);
    }

    @Assign(fields = {"value2", "value1"})
    public void update() {
        System.out.println("Hello");
        changed(this.value2);
    }

    public List<Integer> update2() {
        return changed(this.value2);
    }

    @TrackInitializer(start = true, stop = true)
    public <T> List<T> update3() {
        return (List<T>) changed(this.value2);
    }

    public TestObject1 setValue3(int value1) {
        int value4 = 10;
        int value3 = 11;
        value4 = 15;
        return this;
    }

    public int setValue4() {
        int value4 = 10;
        return value4;
    }
    public static List<Integer> changed(Integer value) {
        value = 100;
        return null;
    }

    @Override
    public String toString() {
        return "com.lexcorp.joura.TestObject{" +
                "value1=" + value1 +
                ", value2=" + value2 +
                '}';
    }
}
