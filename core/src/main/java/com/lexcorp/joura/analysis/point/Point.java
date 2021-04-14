package com.lexcorp.joura.analysis.point;

import java.util.Objects;

import spoon.reflect.code.CtLocalVariable;

public class Point {
    CtLocalVariable<?> localVariable;
    String varName;
    int time;

    public Point(String varName, int time) {
        this.varName = varName;
        this.time = time;
    }

    public Point(CtLocalVariable<?> localVariable, int time) {
        this.localVariable = localVariable;
        this.varName = localVariable.getSimpleName();
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        return varName.equals(point.varName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varName);
    }

    @Override
    public String toString() {
        return "Point( " + varName + " )";
    }
}
