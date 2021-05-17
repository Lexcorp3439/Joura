package com.lexcorp.joura.utils;

import java.util.List;

import com.lexcorp.joura.objects.MethodInvocationTestObject;

public class Methods {
    public int value1213 = 123;

    public void method(Integer id) {
        id += 100;
    }

    public MethodInvocationTestObject create() {
        return new MethodInvocationTestObject();
    }

    public static MethodInvocationTestObject createStatic() {
        return new MethodInvocationTestObject();
    }

    public static void method(Integer i1, int i2, List<Integer> list) {
        list.add(1);
        list.add(2);
        i2 += 3;
        i1 -= 100;
    }

    public static void method(Integer i1, int i2, Integer i3) {
        i2 += 3;
        i1 -= 100;
        i3 = 123;
    }
}
