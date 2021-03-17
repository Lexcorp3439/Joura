package com.lexcorp.joura;

import java.util.List;


public class TestObject1 implements Trackable {
    private int value1 = 0;
    private Integer value2 = 2;
    private boolean value3 = true;

    public int getValue1() {
        return value1;
    }

    public TestObject1 setValue1(int value1) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        this.value1 = value1;
        System.out.println("Hello");
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
