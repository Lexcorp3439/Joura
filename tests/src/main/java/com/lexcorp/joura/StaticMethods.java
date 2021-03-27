package com.lexcorp.joura;

import java.util.List;

public class StaticMethods {

    public static void method(Integer i1, int i2, List<Integer> list) {
        list.add(1);
        list.add(2);
        i2 += 3;
        i1 -= 100;
    }
}
