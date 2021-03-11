package com.lexcorp.joura;

import java.util.List;

@Trackable
public class TestObject implements Trackable1 {
    private int value1 = 0;
    private Integer value2 = 2;

    public int getValue1() {
        return value1;
    }

    public TestObject setValue1(int value1) {
        this.value1 = value1;
        return this;
    }

    public void update() {
        System.out.println("Hello");
        changed(this.value2);
    }

    public List<Integer> update2() {
        return changed(this.value2);
    }

    public <T> List<T> update3() {
        return (List<T>) changed(this.value2);
    }

    public TestObject setValue3(int value1) {
        int value4 = 10;
        int value3 = 11;
        value4 = 15;
        return this;
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
