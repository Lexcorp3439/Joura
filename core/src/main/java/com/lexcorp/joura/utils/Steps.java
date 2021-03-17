package com.lexcorp.joura.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import static com.lexcorp.joura.utils.StringHelper.createFieldName;

public class Steps {

    private final Factory factory;
    private final CtHelper ctHelper;
    private final CtClass<?> ctClass;
    private CtField<?> trackField;

    public Steps(Factory factory, CtClass<?> ctClass) {
        this.factory = factory;
        this.ctHelper = new CtHelper(factory);
        this.ctClass = ctClass;
    }

    public Analyser analyser(Strategy strategy) {
        return new Analyser(strategy, new ArrayList<>(ctClass.getFields()), Collections.singleton(trackField));
    }

    public CtField<Boolean> createClassTrackField(boolean defaultValue) {
        CtTypeReference<Boolean> typeReference = factory.Type().BOOLEAN;
        CtLiteral<Boolean> ctLiteral = ctHelper.createCtLiteral(defaultValue, typeReference);
        CtField<Boolean> field = ctHelper.createCtField(createFieldName(), factory.Type().BOOLEAN, ctLiteral);
        field.setDocComment("Tracking flag");
        trackField = field;
        return field;
    }

    public CtMethod<Void> createTrackMethodIfNotExist(boolean isStart) {
        Set<CtMethod<?>> classMethods = ctClass.getMethods();
        String methodName = isStart ? "startTrack" : "stopTrack";
        List<CtMethod<?>> trackMethods = classMethods.stream()
                .filter(ctMethod -> ctMethod.getSimpleName().equals(methodName))
                .collect(Collectors.toList());

        if (trackMethods.size() == 0) {
            CtMethod<Void> method = this.createTrackMethod(methodName);
            ctClass.addMethod(method);
            return method;
        } else {
            return (CtMethod<Void>) trackMethods.get(0);
        }

    }

    public CtStatement createTrackMethodBody(boolean isStart, CtFieldReference<Boolean> trackFiledReference) {
        CtFieldWrite<Boolean> ctFieldWrite = ctHelper.createCtFieldWrite(factory.createThisAccess(ctClass.getTypeErasure()), trackFiledReference);
        CtLiteral<Boolean> ctLiteral = ctHelper.createCtLiteral(isStart, factory.Type().BOOLEAN);
        return ctHelper.createCtAssignment(ctFieldWrite, ctLiteral, factory.Type().BOOLEAN);
    }

    public CtStatement createFieldChangeNotifierStatement(
            CtFieldReference<Boolean> trackFiledReference,
            CtMethod<?> method,
            Collection<CtField<?>> fieldList
    ) {
        CtBlock<?> thenStatement = factory.createBlock();
        CtFieldRead<Boolean> condition = ctHelper.createCtFieldRead(factory.createThisAccess(ctClass.getTypeErasure()), trackFiledReference);
        CtStatement mapStatement = ctHelper.createFormatCodeSnippet("java.util.Map<String, Object> map123456678 = new java.util.HashMap<>()");
        thenStatement.addStatement(mapStatement);

        for (CtField<?> field : fieldList) {
            thenStatement.addStatement(ctHelper.createFormatCodeSnippet("map123456678.put(\"%s\", this.%s)", field.getSimpleName(), field.getSimpleName()));
        }
        CtStatement invocationStatement = ctHelper.createFormatCodeSnippet(
                "com.lexcorp.joura.listeners.FiledChangeListener.getInstance().accept(this, \"%s\", map123456678)",
                method.getSimpleName()
        );
        thenStatement.addStatement(invocationStatement);
        return ctHelper.createCtIf(condition, thenStatement, null);

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
