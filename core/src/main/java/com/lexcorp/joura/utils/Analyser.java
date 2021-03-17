package com.lexcorp.joura.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtFieldReference;

public class Analyser {
    private final Collection<CtField<?>> classFields;
    private final Strategy strategy;

    public Analyser( Strategy strategy, Collection<CtField<?>> classFields, Collection<CtField<?>> ignoreClassFields) {
        classFields.removeAll(ignoreClassFields);
        this.classFields = classFields;
        this.strategy = strategy;
    }

    public Collection<CtField<?>> getEditableFieldsFromMethod(CtMethod<?> method) {
        switch (strategy) {
            case NONE:
                return this.classFields;
            case DEFAULT:
                return getEditableFieldsFromMethodWithLiteInvocationAnalysis(method);
            case DEEP:
                return getEditableFieldsFromMethodWithDeepInvocationAnalysis(method);
            default:
                throw new IllegalArgumentException("Unexpected strategy for analysing");
        }
    }

    private List<CtField<?>> getEditableFieldsFromMethodWithLiteInvocationAnalysis(CtMethod<?> method) {
        Set<CtField<?>> editableFields = new HashSet<>();
        method.getElements(e -> e instanceof CtInvocation).stream()
                .map(e -> e.getElements(element -> element instanceof CtFieldReference))
                .forEach(ctElementList -> {
                    ctElementList.forEach(ctElement ->
                            editableFields.add((((CtFieldRead<?>) ctElement.getParent()).getVariable()).getFieldDeclaration()));
                });
        method.getElements(e -> e instanceof CtFieldWrite).forEach(ctElement -> {
                    editableFields.add(((CtFieldWrite<?>) ctElement).getVariable().getFieldDeclaration());
                }
        );
        editableFields.retainAll(classFields);
        return new ArrayList<>(editableFields);
    }

    private List<CtField<?>> getEditableFieldsFromMethodWithDeepInvocationAnalysis(CtMethod<?> method) {
        return getEditableFieldsFromMethodWithLiteInvocationAnalysis(method);
    }

}
