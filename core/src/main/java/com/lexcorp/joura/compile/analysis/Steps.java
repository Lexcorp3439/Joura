package com.lexcorp.joura.compile.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.lexcorp.joura.runtime.options.Assign;
import com.lexcorp.joura.runtime.options.Strategy;
import com.lexcorp.joura.runtime.options.TrackField;
import com.lexcorp.joura.runtime.options.TrackInitializer;
import com.lexcorp.joura.runtime.options.Untracked;
import com.lexcorp.joura.utils.CtHelper;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
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
    private Analyser analyser;

    public Steps(Factory factory, CtClass<?> ctClass) {
        this.factory = factory;
        this.ctHelper = new CtHelper(factory);
        this.ctClass = ctClass;
        this.analyser = new Analyser(this.getFields(), Collections.singleton(trackField));
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
        if (methods == null) {
            methods = ctClass.getMethods().stream()
                    .filter(m -> !m.isStatic())
                    .filter(m -> !m.hasAnnotation(Untracked.class))
                    .collect(Collectors.toList());
        }
        return methods;
    }

    public Analyser analyser(Strategy strategy) {
        return analyser.setStrategy(strategy);
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

    public CtField<String> createIdentifierField() {
        CtTypeReference<String> typeReference = factory.Type().STRING;
        CtLiteral<String> ctLiteral = ctHelper.createCtLiteral("UNKNOWN", typeReference);
        identifierField = ctHelper.createCtField(createFieldName(), factory.Type().STRING, ctLiteral);
        identifierField.setDocComment("Identifier");
        return identifierField;
    }

    public CtMethod<Void> createSetIdentifierMethod() {
        CtParameter<String> parameter = ctHelper.createCtParameter(factory.Type().STRING, "newIdentifier");
        CtMethod<Void> setIdentifierMethod = ctHelper.createMethodWithParameters(
                "setIdentifier", factory.Type().VOID_PRIMITIVE,
                Collections.singleton(ModifierKind.PUBLIC), List.of(parameter));
        CtFieldWrite<String> ctFieldWrite = ctHelper.createCtFieldWrite(
                factory.createThisAccess(ctClass.getTypeErasure()), this.identifierField.getReference()
        );

        CtVariableRead<String> ctVariableRead = ctHelper.createCtVariableRead(parameter.getReference(), false);
        CtAssignment<String, String> ctAssignment = ctHelper.createCtAssignment(ctFieldWrite, ctVariableRead, factory.Type().STRING);
        setIdentifierMethod.getBody().insertBegin(ctAssignment);
        return setIdentifierMethod;
    }

    public CtMethod<String> createGetIdentifierMethod() {
        CtMethod<String> getIdentifierMethod = ctHelper.createMethod(
                "getIdentifier", factory.Type().STRING, Collections.singleton(ModifierKind.PUBLIC));
        CtFieldRead<String> ctFieldRead = ctHelper.createCtFieldRead(ctClass, identifierField);
        CtReturn<String> ctReturn = ctHelper.createCtReturn(ctFieldRead);
        getIdentifierMethod.getBody().insertEnd(ctReturn);
        return getIdentifierMethod;
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
                "com.lexcorp.joura.runtime.listeners.FieldChangeListener.getInstance().accept(this, \"%s\", map123456678)",
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
            method.getBody().removeStatement(ctReturn);
            if (ctReturn.getElements(e -> e instanceof CtInvocation).size() > 0) {

                CtLocalVariable<?> localVariable = ctHelper.createLocalVar(
                        (CtTypeReference) method.getType(), ctReturn.getReturnedExpression()
                );
                CtVariableRead variableRead = ctHelper.createCtVariableRead(localVariable.getReference(), false);
                CtReturn<?> ctReturnStatement = factory.createReturn().setReturnedExpression(variableRead);

                method.getBody().insertEnd(localVariable);
                method.getBody().insertEnd(ctStatement);
                method.getBody().insertEnd(ctReturnStatement);
            } else {
                method.getBody().insertEnd(ctStatement);
                method.getBody().insertEnd(ctReturn);
            }
        });
    }


    private CtMethod<Void> createTrackMethod(String methodName) {
        return ctHelper.createMethod(methodName, factory.Type().VOID_PRIMITIVE, Collections.singleton(ModifierKind.PUBLIC));
    }

}
