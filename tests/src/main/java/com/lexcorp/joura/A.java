package com.lexcorp.joura;

public class A {
    static class B {
        public int i = 100;

        public B(int i) {
            this.i = i;
        }
    }

    public static void main(String[] args) {
        B b1 = new B(10);
        B b2 = new B(200);
        B ref1 = b1;
        B ref2 = ref1;
        System.out.println(ref2.i);
        ref1 = b2;
        System.out.println(ref2.i);
    }
}
