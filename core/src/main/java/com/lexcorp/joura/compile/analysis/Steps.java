package com.lexcorp.joura.compile.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lexcorp.joura.compile.analysis.generators.FieldsGenerator;
import com.lexcorp.joura.compile.analysis.generators.MethodsGenerator;
import com.lexcorp.joura.compile.analysis.strategies.AbstractStrategy;
import com.lexcorp.joura.compile.analysis.strategies.AnalysisStrategy;
import com.lexcorp.joura.runtime.options.Assign;
import com.lexcorp.joura.runtime.options.Strategy;
import com.lexcorp.joura.runtime.options.Untracked;
import com.lexcorp.joura.utils.CtHelper;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;

public class Steps {
    private final Factory factory;
    private final CtHelper ctHelper;
    private final CtClass<?> ctClass;
    private List<CtField<?>> fields;
    private List<CtMethod<?>> methods;
    private AbstractStrategy strategy;
    private final FieldsGenerator fieldsGenerator;
    private final MethodsGenerator methodsGenerator;

    public Steps(Factory factory, CtClass<?> ctClass) {
        this.factory = factory;
        this.ctHelper = new CtHelper(factory);
        this.ctClass = ctClass;
        this.methodsGenerator = new MethodsGenerator(this.ctClass, getTrackedMethods());
        this.fieldsGenerator = new FieldsGenerator(this.ctClass, getFields());
    }

    public FieldsGenerator fieldsGenerator() {
        return fieldsGenerator;
    }

    public MethodsGenerator methodsGenerator() {
        return methodsGenerator;
    }

    public List<CtField<?>> getFields() {
        if (fields == null) {
            fields = new ArrayList<>(ctClass.getFields()).stream()
                    .filter(f -> !f.hasAnnotation(Untracked.class))
                    .collect(Collectors.toList());
        }
        return fields;
    }

    public List<CtField<?>> getAssignedFields(CtMethod<?> method) {
        Set<String> assignedFields = Set.of(method.getAnnotation(Assign.class).fields());
        return getFields().stream()
                .filter(f -> assignedFields.contains(f.getSimpleName()))
                .collect(Collectors.toList());
    }

    public List<CtMethod<?>> getTrackedMethods() {
        Set<String> excludedMethods = new HashSet<>() {{
            add("toString");
            add("hashCode");
            add("equals");
        }};
        if (methods == null) {
            methods = ctClass.getMethods().stream()
                    .filter(m -> !m.isStatic())
                    .filter(m -> !excludedMethods.contains(m.getSimpleName()))
                    .filter(m -> !m.hasAnnotation(Untracked.class))
                    .collect(Collectors.toList());
        }
        return methods;
    }

    public AnalysisStrategy analyser() {
        return this.strategy;
    }

    public void setUpAnalyser(Strategy rawStrategy) {
        this.strategy = ((AbstractStrategy) rawStrategy.getStrategy())
                .setClassFields(this.getFields())
                .setCtClass(this.ctClass);
    }

    public CtStatement createFieldChangeNotifierStatement(
            CtFieldReference<Boolean> trackFiledReference,
            CtMethod<?> method,
            Collection<CtField<?>> fieldList
    ) {
        CtBlock<?> thenStatement = factory.createBlock();
        CtFieldRead<Boolean> condition = ctHelper.createCtFieldRead(
                factory.createThisAccess(ctClass.getTypeErasure()), trackFiledReference
        );

        StringBuilder codeSnippet = new StringBuilder();
        codeSnippet.append("java.util.Map<String, Object> map123456678 = com.lexcorp.joura.utils.StringHelper.mapOf(");
        fieldList.forEach(field ->
                codeSnippet.append(String.format("\"%s\", this.%s,", field.getSimpleName(), field.getSimpleName()))
        );
        codeSnippet.deleteCharAt(codeSnippet.length() - 1);
        codeSnippet.append(")");
        CtStatement mapStatement = ctHelper.createFormatCodeSnippet(codeSnippet.toString());
        thenStatement.addStatement(mapStatement);

        CtStatement invocationStatement = ctHelper.createFormatCodeSnippet(
                "com.lexcorp.joura.runtime.listeners.FieldChangeReceiver.getInstance().accept(this, \"%s\", map123456678)",
                method.getSignature()
        );
        thenStatement.addStatement(invocationStatement);
        return ctHelper.createCtIf(condition, thenStatement, null);

    }
}
