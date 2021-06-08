package com.lexcorp.joura.compile.analysis.strategies.simple;

import java.util.List;

import com.lexcorp.joura.compile.analysis.strategies.AbstractStrategy;

import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

public class SimpleStrategy extends AbstractStrategy {

    @Override
    public void run() {
    }

    @Override
    public List<CtField<?>> runForMethod(CtMethod<?> method) {
        return this.classFields;
    }
}
