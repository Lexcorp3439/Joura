package com.lexcorp.joura;

import java.util.List;

public class StaticMethods {
    public int value1213 = 123;

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
