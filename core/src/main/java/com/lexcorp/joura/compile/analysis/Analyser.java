package com.lexcorp.joura.compile.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lexcorp.joura.compile.analysis.alias.AliasAnalysis;
import com.lexcorp.joura.compile.analysis.alias.Aliases;
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
import spoon.reflect.declaration.CtElement;
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
        aliasAnalysis.run();
        if (method.getSimpleName().equals("referenceMethod")) {
            System.out.println();
        }
        method.getElements(e -> e instanceof CtAssignment)
                .forEach(e -> this.addUpdatedFieldsForCurrentIteration(
                        (CtAssignment<?, ?>) e, editableFields, aliasAnalysis.method(method)
                ));
        method.getElements(e -> e instanceof CtInvocation).stream()
                .map(e -> e.getElements(element -> element instanceof CtFieldReference))
                .forEach(e -> this.addFieldsPassedToMethod(e, editableFields, aliasAnalysis.method(method)));
        editableFields.retainAll(classFields);
        return new ArrayList<>(editableFields);

    }

    private List<CtField<?>> getEditableFieldsFromMethodWithDeepInvocationAnalysis(CtMethod<?> method) {
        return getEditableFieldsFromMethodWithLiteInvocationAnalysis(method);
    }

    /**
     * Метод анализирует и добавляет те поля, которые ссылаются на this и были изменены присвоением
     *
     * @param ctAssignment   текущий ctAssignment кода
     * @param editableFields множество изменненных полей в методе
     */
    private void addUpdatedFieldsForCurrentIteration(
            CtAssignment<?, ?> ctAssignment, Set<CtField<?>> editableFields, Aliases aliases) {
        CtExpression<?> assigned = ctAssignment.getAssigned();
        if (assigned instanceof CtFieldWrite) {
            CtFieldWrite<?> fieldWrite = (CtFieldWrite<?>) assigned;
            CtField<?> field = fieldWrite.getVariable().getFieldDeclaration();
            if (fieldWrite.getTarget() instanceof CtVariableRead) {
                if (fieldWrite.getTarget() instanceof CtFieldRead) {
                    CtFieldRead<?> ctFieldRead = (CtFieldRead<?>) fieldWrite.getTarget();
                    if (aliasAnalysis.isTrackable(ctFieldRead.getVariable().getType())) {
                        editableFields.add(field);
                    }
                } else {
//                    String varName = ((CtVariableRead<?>) fieldWrite.getTarget()).getVariable().getSimpleName();
                    String reference = fieldWrite.getTarget().toString();
                    boolean is_method_alias = aliases.isThisOrUnknownAlias(reference);
                    boolean is_class_field_alias = aliasAnalysis.fieldAliases.isThisOrUnknownAlias(reference);
                    if (is_method_alias || is_class_field_alias) {
                        editableFields.add(field);
                    }
                }
            } else {
                editableFields.add(field);
            }
        }
    }

    /**
     * Метод анализирует и добавляет те поля, которые ссылаются на this и переданы в какой-либо метод
     *
     * @param ctElementList  список
     * @param editableFields множество изменненных полей в методе
     */
    private void addFieldsPassedToMethod(
            List<CtElement> ctElementList, Set<CtField<?>> editableFields, Aliases aliases
    ) {
        ctElementList.forEach(ctElement -> {
            CtFieldRead<?> fieldRead = (CtFieldRead<?>) ctElement.getParent();
            CtField<?> field = fieldRead.getVariable().getFieldDeclaration();
            if (fieldRead.getTarget() instanceof CtThisAccess) {
                editableFields.add(field);
            } else if (fieldRead.getTarget() instanceof CtVariableRead) {
                if (fieldRead.getTarget() instanceof CtFieldRead) {
                    CtFieldRead<?> ctFieldRead = (CtFieldRead<?>) fieldRead.getTarget();
                    if (aliasAnalysis.isTrackable(ctFieldRead.getVariable().getType())) {
                        editableFields.add(field);
                    }
                } else {
                    String reference = fieldRead.getTarget().toString();
                    boolean is_method_alias = aliases.isThisOrUnknownAlias(reference);
                    boolean is_class_field_alias = aliasAnalysis.fieldAliases.isThisOrUnknownAlias(reference);
                    if (is_method_alias || is_class_field_alias) {
                        editableFields.add(field);
                    }
                }
            }
        });
    }

}
