package com.lexcorp.joura.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lexcorp.joura.analysis.point.PointAnalysis;
import com.lexcorp.joura.options.Strategy;

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
    private final Strategy strategy;

    public Analyser(Strategy strategy, List<CtField<?>> classFields, Collection<CtField<?>> ignoreClassFields) {
        classFields.removeAll(ignoreClassFields);
        this.classFields = classFields;
        this.strategy = strategy;
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
        PointAnalysis pointAnalysis = new PointAnalysis(classFields.get(0).getParent(CtClass.class), method);
        pointAnalysis.run();
        method.getBody().getStatements().forEach(ctStatement -> {
            this.addUpdatedFieldsForCurrentIteration(ctStatement, pointAnalysis, editableFields);
            this.addFieldsPassedToMethod(ctStatement, pointAnalysis, editableFields);
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
     * @param pointAnalysis  инстанс анализатора указателей
     * @param editableFields множество изменненных полей в методе
     */
    private void addUpdatedFieldsForCurrentIteration(
            CtStatement ctStatement, PointAnalysis pointAnalysis, Set<CtField<?>> editableFields) {
        if (ctStatement instanceof CtAssignment) {
            CtExpression<?> assignment = ((CtAssignment<?, ?>) ctStatement).getAssigned();
            if (assignment instanceof CtFieldWrite) {
                CtFieldWrite<?> fieldWrite = (CtFieldWrite<?>) assignment;
                CtField<?> field = fieldWrite.getVariable().getFieldDeclaration();
                if (fieldWrite.getTarget() instanceof CtVariableRead) {
                    String varName = ((CtVariableRead<?>) fieldWrite.getTarget()).getVariable().getSimpleName();
                    if (pointAnalysis.isThisPointer(varName)) {
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
     * @param pointAnalysis  инстанс анализатора указателей
     * @param editableFields множество изменненных полей в методе
     */
    private void addFieldsPassedToMethod(
            CtStatement ctStatement, PointAnalysis pointAnalysis, Set<CtField<?>> editableFields
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
                        if (pointAnalysis.isThisPointer(varName)) {
                            editableFields.add(field);
                        }
                    }
                }));
    }

}
