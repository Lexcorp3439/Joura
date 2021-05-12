package com.lexcorp.joura.compile.analysis.strategies;

import java.util.List;

import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

public class SimpleStrategy extends AbstractStrategy {

    @Override
    public List<CtField<?>> runForMethod(CtMethod<?> method) {
        return this.classFields;
    }
}
