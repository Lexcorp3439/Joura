package com.lexcorp.joura.compile.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lexcorp.joura.compile.analysis.alias.AliasAnalysis;
import com.lexcorp.joura.runtime.options.Strategy;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtFieldReference;

public class Analyser {
    private final List<CtField<?>> classFields;
    private Strategy strategy;
    private final AliasAnalysis aliasAnalysis;

    public Analyser(Strategy strategy, List<CtField<?>> classFields, Collection<CtField<?>> ignoreClassFields) {
        classFields.removeAll(ignoreClassFields);
        this.classFields = classFields;
        this.strategy = strategy;
        this.aliasAnalysis = new AliasAnalysis(classFields.get(0).getParent(CtClass.class));
    }

    public Analyser(List<CtField<?>> classFields, Collection<CtField<?>> ignoreClassFields) {
        classFields.removeAll(ignoreClassFields);
        this.classFields = classFields;
        this.strategy = null;
        this.aliasAnalysis = new AliasAnalysis(classFields.get(0).getParent(CtClass.class));
    }

    public Analyser setStrategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public List<CtField<?>> getEditableFieldsFromMethod(CtMethod<?> method) {
        if (this.classFields.isEmpty()) {
            return Collections.emptyList();
        }
        switch (strategy) {
            case NONE:
                return this.classFields;
            case LAZY:
                return getEditableFieldsFromMethodWithLazyInvocationAnalysis(method);
            case DEFAULT:
                return getEditableFieldsFromMethodWithLiteInvocationAnalysis(method);
            case DEEP:
                return getEditableFieldsFromMethodWithDeepInvocationAnalysis(method);
            default:
                throw new IllegalArgumentException("Unexpected strategy for analysing");
        }
    }

    private List<CtField<?>> getEditableFieldsFromMethodWithLazyInvocationAnalysis(CtMethod<?> method) {
        Set<CtField<?>> editableFields = new HashSet<>();
        method.getElements(e -> e instanceof CtInvocation).stream()
                .map(e -> e.getElements(element -> element instanceof CtFieldReference))
                .forEach(ctElementList -> ctElementList.forEach(ctElement -> {
                    CtField<?> field = (((CtFieldRead<?>) ctElement.getParent()).getVariable()).getFieldDeclaration();
                    editableFields.add(field);
                }));
        method.getElements(e -> e instanceof CtFieldWrite).forEach(ctElement ->
                editableFields.add(((CtFieldWrite<?>) ctElement).getVariable().getFieldDeclaration())
        );
        editableFields.retainAll(classFields);
        return editableFields.stream()
                .filter(candidate ->
                        candidate.getParent(CtClass.class).equals(classFields.get(0).getParent(CtClass.class))
                )
                .collect(Collectors.toList());
    }


    private List<CtField<?>> getEditableFieldsFromMethodWithLiteInvocationAnalysis(CtMethod<?> method) {
        Set<CtField<?>> editableFields = new HashSet<>();
        aliasAnalysis.runForMethod(method);
        method.getBody().getStatements().forEach(ctStatement -> {
            this.addUpdatedFieldsForCurrentIteration(ctStatement, editableFields);
            this.addFieldsPassedToMethod(ctStatement, editableFields);
        });
        editableFields.retainAll(classFields);
        return new ArrayList<>(editableFields);

    }

    private List<CtField<?>> getEditableFieldsFromMethodWithDeepInvocationAnalysis(CtMethod<?> method) {
        return getEditableFieldsFromMethodWithLiteInvocationAnalysis(method);
    }

    /**
     * Метод анализирует и добавляет те поля, которые ссылаются на this и были изменены присвоением
     *
     * @param ctStatement    текущий ctStatement кода
     * @param editableFields множество изменненных полей в методе
     */
    private void addUpdatedFieldsForCurrentIteration(
            CtStatement ctStatement, Set<CtField<?>> editableFields) {
        if (ctStatement instanceof CtAssignment) {
            CtExpression<?> assignment = ((CtAssignment<?, ?>) ctStatement).getAssigned();
            if (assignment instanceof CtFieldWrite) {
                CtFieldWrite<?> fieldWrite = (CtFieldWrite<?>) assignment;
                CtField<?> field = fieldWrite.getVariable().getFieldDeclaration();
                if (fieldWrite.getTarget() instanceof CtVariableRead) {
//                    String varName = ((CtVariableRead<?>) fieldWrite.getTarget()).getVariable().getSimpleName();
                    if (aliasAnalysis.isThisAlias(fieldWrite.getTarget().toString())) {
                        editableFields.add(field);
                    }
                } else {
                    editableFields.add(field);
                }
            }
        }
    }

    /**
     * Метод анализирует и добавляет те поля, которые ссылаются на this и переданы в какой-либо метод
     *
     * @param ctStatement    текущий ctStatement кода
     * @param editableFields множество изменненных полей в методе
     */
    private void addFieldsPassedToMethod(
            CtStatement ctStatement, Set<CtField<?>> editableFields
    ) {
        ctStatement.getElements(e -> e instanceof CtInvocation).stream()
                .map(e -> e.getElements(element -> element instanceof CtFieldReference))
                .forEach(ctElementList -> ctElementList.forEach(ctElement -> {
                    CtFieldRead<?> fieldRead = (CtFieldRead<?>) ctElement.getParent();
                    CtField<?> field = fieldRead.getVariable().getFieldDeclaration();
                    if (fieldRead.getTarget() instanceof CtThisAccess) {
                        editableFields.add(field);
                    } else if (fieldRead.getTarget() instanceof CtVariableRead) {
                        String varName = ((CtVariableRead<?>) fieldRead.getTarget()).getVariable().getSimpleName();
                        if (aliasAnalysis.isThisAlias(varName)) {
                            editableFields.add(field);
                        }
                    }
                }));
    }

}
