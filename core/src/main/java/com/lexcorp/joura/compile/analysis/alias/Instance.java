package com.lexcorp.joura.compile.analysis.alias;

public class Instance {
    Type type;

    final static Instance THIS = new Instance(Type.THIS);


    public Instance(Type type) {
        this.type = type;
    }

    enum Type {
        THIS, UNKNOWN, NEW
    }
}
