package com.lexcorp.joura.compile.analysis.generators;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lexcorp.joura.compile.analysis.strategies.AbstractStrategy;
import com.lexcorp.joura.runtime.options.TrackInitializer;
import com.lexcorp.joura.utils.CtHelper;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtLiteral;
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

public class MethodsGenerator {
    private final Factory factory;
    private final CtHelper ctHelper;
    private final CtClass<?> ctClass;
    private CtField<Boolean> trackField;
    private CtField<String> identifierField;
    private List<CtField<?>> fields;
    private List<CtMethod<?>> methods;
    private AbstractStrategy strategy;

    public MethodsGenerator(CtClass<?> ctClass, List<CtMethod<?>> trackedMethods) {
        this.factory = ctClass.getFactory();
        this.ctHelper = new CtHelper(factory);
        this.ctClass = ctClass;
        this.methods = trackedMethods;
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
        Set<CtMethod<?>> classMethods = new HashSet<>(this.methods);
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

    private CtMethod<Void> createTrackMethod(String methodName) {
        return ctHelper.createMethod(methodName, factory.Type().VOID_PRIMITIVE, Collections.singleton(ModifierKind.PUBLIC));
    }
}
