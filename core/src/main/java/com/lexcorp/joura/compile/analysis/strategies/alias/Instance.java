package com.lexcorp.joura.compile.analysis.strategies.alias;

import java.util.Objects;

public class Instance {
    Type type;

    final static Instance THIS = new Instance(Type.THIS);


    public Instance(Type type) {
        this.type = type;
    }

    enum Type {
        THIS, UNKNOWN, NEW, ARGUMENT
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Instance instance = (Instance) o;
        return type == instance.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "" + type;
    }
}
