package com.lexcorp.joura;

import java.util.HashMap;
import java.util.List;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.options.Strategy;
import com.lexcorp.joura.runtime.options.TrackField;
import com.lexcorp.joura.runtime.options.TrackOptions;
import com.lexcorp.joura.runtime.options.Untracked;


@TrackOptions(alwaysTrack = true, analysingStrategy = Strategy.NONE)
public class TestObject2 implements Trackable {
    private int value1 = 0;
    @Untracked
    private Integer value2 = 2;
    @TrackField
    private boolean value3 = true;

    public int getValue1() {
        return value1;
    }

    public TestObject2 setValue1(int value1) {
        java.util.Map<String, Object> map = new HashMap<>();
        this.value1 = value1;
        System.out.println("Hello");
        return this;
    }

    public void update() {
        System.out.println("Hello");
        changed(this.value2);
    }

    @Untracked
    public List<Integer> update2() {
        return changed(this.value2);
    }

    public <T> List<T> update3() {
        return (List<T>) changed(this.value2);
    }

    public TestObject2 setValue3(int value1) {
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
