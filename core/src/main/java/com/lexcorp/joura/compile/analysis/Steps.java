package com.lexcorp.joura.compile.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.lexcorp.joura.compile.analysis.generators.FieldsGenerator;
import com.lexcorp.joura.compile.analysis.generators.MethodsGenerator;
import com.lexcorp.joura.compile.analysis.strategies.AbstractStrategy;
import com.lexcorp.joura.compile.analysis.strategies.AnalysisStrategy;
import com.lexcorp.joura.runtime.options.Assign;
import com.lexcorp.joura.runtime.options.Strategy;
import com.lexcorp.joura.runtime.options.TrackField;
import com.lexcorp.joura.runtime.options.TrackInitializer;
import com.lexcorp.joura.runtime.options.Untracked;
import com.lexcorp.joura.utils.CtHelper;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import static com.lexcorp.joura.utils.StringHelper.createFieldName;

public class Steps {
    private final Factory factory;
    private final CtHelper ctHelper;
    private final CtClass<?> ctClass;
    private CtField<Boolean> trackField;
    private CtField<String> identifierField;
    private List<CtField<?>> fields;
    private List<CtMethod<?>> methods;
    private AbstractStrategy strategy;
    private FieldsGenerator fieldsGenerator;
    private MethodsGenerator methodsGenerator;

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

    public CtField<Boolean> createClassTrackFieldIfNotAssigned(boolean defaultValue) {
        List<CtField<?>> ctFields = getFields().stream()
                .filter(f -> f.hasAnnotation(TrackField.class))
                .collect(Collectors.toList());
        if (ctFields.size() > 1) {
            throw new RuntimeException("Number of Track fields should not exceed 1");
        }
        if (ctFields.isEmpty()) {
            CtTypeReference<Boolean> typeReference = factory.Type().BOOLEAN;
            CtLiteral<Boolean> ctLiteral = ctHelper.createCtLiteral(defaultValue, typeReference);
            trackField = ctHelper.createCtField(createFieldName(), factory.Type().BOOLEAN, ctLiteral);
        } else {
            if (ctFields.get(0).getType().equals(factory.Type().BOOLEAN)
                    || ctFields.get(0).getType().equals(factory.Type().BOOLEAN_PRIMITIVE)) {
                trackField = (CtField<Boolean>) ctFields.get(0);
            } else {
                throw new RuntimeException("Type of Track fields should be boolean");
            }
        }
        trackField.setDocComment("Tracking flag");
        return trackField;
    }

    public CtField<String> createTagField() {
        CtTypeReference<String> typeReference = factory.Type().STRING;
        CtLiteral<String> ctLiteral = ctHelper.createCtLiteral("UNKNOWN", typeReference);
        CtField<?> field = ctHelper.createCtField(createFieldName(), factory.Type().STRING, ctLiteral);
        identifierField = ctHelper.createCtField(createFieldName(), factory.Type().STRING, ctLiteral);
        identifierField.setDocComment("Tag");
        return identifierField;
    }

    public CtMethod<Void> createSetTagMethod() {
        CtParameter<String> parameter = ctHelper.createCtParameter(factory.Type().STRING, "newIdentifier");
        CtMethod<Void> setTagMethod = ctHelper.createMethodWithParameters(
                "setTag", factory.Type().VOID_PRIMITIVE,
                Collections.singleton(ModifierKind.PUBLIC), List.of(parameter));
        CtFieldWrite<String> ctFieldWrite = ctHelper.createCtFieldWrite(
                factory.createThisAccess(ctClass.getTypeErasure()), this.identifierField.getReference()
        );

        CtVariableRead<String> ctVariableRead = ctHelper.createCtVariableRead(parameter.getReference(), false);
        CtAssignment<String, String> ctAssignment = ctHelper.createCtAssignment(ctFieldWrite, ctVariableRead, factory.Type().STRING);
        setTagMethod.getBody().insertBegin(ctAssignment);
        return setTagMethod;
    }

    public CtMethod<String> createGetTagMethod() {
        CtMethod<String> getTagMethod = ctHelper.createMethod(
                "getTag", factory.Type().STRING, Collections.singleton(ModifierKind.PUBLIC));
        CtFieldRead<String> ctFieldRead = ctHelper.createCtFieldRead(ctClass, identifierField);
        CtReturn<String> ctReturn = ctHelper.createCtReturn(ctFieldRead);
        getTagMethod.getBody().insertEnd(ctReturn);
        return getTagMethod;
    }

    public CtMethod<?> createTrackMethodIfNotExist(boolean isStart) {
        Set<CtMethod<?>> classMethods = new HashSet<>(this.getTrackedMethods());
        String methodName = isStart ? "startTrack" : "stopTrack";
        List<CtMethod<?>> defaultTrackMethods = classMethods.stream()
                .filter(ctMethod -> ctMethod.getSimpleName().equals(methodName))
                .collect(Collectors.toList());

        List<CtMethod<?>> userTrackMethods = classMethods.stream()
                .filter(ctMethod ->
                        ctMethod.hasAnnotation(TrackInitializer.class) && (
                                ctMethod.getAnnotation(TrackInitializer.class).start() == isStart
                                        || ctMethod.getAnnotation(TrackInitializer.class).stop() == !isStart)
                )
                .collect(Collectors.toList());

        if (defaultTrackMethods.isEmpty() && userTrackMethods.isEmpty()) {
            CtMethod<Void> method = this.createTrackMethod(methodName);
            ctClass.addMethod(method);
            return method;
        } else {
            if (userTrackMethods.isEmpty()) {
                return defaultTrackMethods.get(0);
            } else {
                return userTrackMethods.get(0);
            }
        }

    }

    public CtStatement createTrackMethodBody(boolean isStart) {
        CtFieldWrite<Boolean> ctFieldWrite = ctHelper.createCtFieldWrite(
                factory.createThisAccess(ctClass.getTypeErasure()), this.trackField.getReference()
        );
        CtLiteral<Boolean> ctLiteral = ctHelper.createCtLiteral(isStart, factory.Type().BOOLEAN);
        return ctHelper.createCtAssignment(ctFieldWrite, ctLiteral, factory.Type().BOOLEAN);
    }

    public CtStatement createTrackMethodBodyWithInvert() {
        CtFieldWrite<Boolean> ctFieldWrite = ctHelper.createCtFieldWrite(
                factory.createThisAccess(ctClass.getTypeErasure()), this.trackField.getReference()
        );
        CtUnaryOperator<Boolean> unaryOperator = factory.createUnaryOperator();
        unaryOperator.setOperand(ctHelper.createCtFieldRead(ctClass, trackField));
        unaryOperator.setKind(UnaryOperatorKind.NOT);
        unaryOperator.setType(factory.Type().BOOLEAN);
        return ctHelper.createCtAssignment(ctFieldWrite, unaryOperator, factory.Type().BOOLEAN);
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

    public void updateMethodWithStatement(CtMethod<?> method, CtStatement ctStatement) {
        if (method.getType().equals(factory.Type().VOID_PRIMITIVE)) {
            method.getBody().insertEnd(ctStatement);
        } else {
            this.updateReturnStatements(method, ctStatement);
        }

    }

    public void updateReturnStatements(CtMethod<?> method, CtStatement ctStatement) {
        List<CtReturn<?>> ctReturns = method.getElements(Objects::nonNull);
        ctReturns.forEach(ctReturn -> {
            ctStatement.setParent(ctReturn.getParent());
            CtBlock<?> ctBlock = ctReturn.getParent(CtBlock.class);
            ctBlock.removeStatement(ctReturn);
            if (ctReturn.getElements(e -> e instanceof CtInvocation).size() > 0) {
                CtLocalVariable<?> localVariable = ctHelper.createLocalVar(
                        (CtTypeReference) method.getType(), ctReturn.getReturnedExpression()
                );
                CtVariableRead variableRead = ctHelper.createCtVariableRead(localVariable.getReference(), false);
                CtReturn<?> ctReturnStatement = factory.createReturn().setReturnedExpression(variableRead);

                ctBlock.insertEnd(localVariable);
                ctBlock.insertEnd(ctStatement);
                ctBlock.insertEnd(ctReturnStatement);
            } else {
                try {
                    ctBlock.insertEnd(ctStatement);
                    ctBlock.insertEnd(ctReturn);
                } catch (spoon.SpoonException ignored) {
                }
            }
        });
    }


    private CtMethod<Void> createTrackMethod(String methodName) {
        return ctHelper.createMethod(methodName, factory.Type().VOID_PRIMITIVE, Collections.singleton(ModifierKind.PUBLIC));
    }

}
