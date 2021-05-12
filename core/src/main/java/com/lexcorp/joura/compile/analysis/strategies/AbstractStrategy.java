package com.lexcorp.joura.compile.analysis.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

public abstract class AbstractStrategy implements AnalysisStrategy {
    protected List<CtField<?>> classFields;
    protected CtClass<?> ctClass;

    public AbstractStrategy() {
    }

    public AbstractStrategy setClassFields(List<CtField<?>> classFields) {
        this.classFields = classFields;
        return this;
    }

    public AbstractStrategy setCtClass(CtClass<?> ctClass) {
        this.ctClass = ctClass;
        return this;
    }

    public AbstractStrategy(CtClass<?> ctClass, List<CtField<?>> classFields, Collection<CtField<?>> ignoreClassFields) {
        classFields.removeAll(ignoreClassFields);
        this.classFields = classFields;
        this.ctClass = ctClass;
    }

    @Override
    public Map<CtMethod<?>, List<CtField<?>>> runForClass(boolean allMethods) {
        Map<CtMethod<?>, List<CtField<?>>> methodsEditableFields = new HashMap<>();
        Set<CtMethod<?>> methods = allMethods ? ctClass.getAllMethods() : ctClass.getMethods();
        for (CtMethod<?> method : methods) {
            methodsEditableFields.put(method, this.runForMethod(method));
        }
        return methodsEditableFields;
    }

}
