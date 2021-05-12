package com.lexcorp.joura.compile.analysis.strategies;

import java.util.List;
import java.util.Map;

import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

public interface AnalysisStrategy {
    List<CtField<?>> runForMethod(CtMethod<?> method);

    Map<CtMethod<?>, List<CtField<?>>> runForClass(boolean allMethods);
}
