package com.lexcorp.joura.utils;

import java.util.Collections;
import java.util.List;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;

public class TrackCodeBuilder {
    public static CtIf buildTrackExpression(
            Factory factory,
            CtClass<?> ctClass,
            CtFieldReference<Boolean> trackFiledReference,
            CtMethod<?> method,
            List<CtField<?>> fieldList
    ) {
        CtHelper helper = new CtHelper(factory);
        CtBlock<?> thenStatement = factory.createBlock();
        CtFieldRead<Boolean> condition = helper.createCtFieldRead(factory.createThisAccess(ctClass.getTypeErasure()), trackFiledReference);
        CtStatement mapStatement = helper.createFormatCodeSnippet("java.util.Map<String, Object> map123456678 = new java.util.HashMap<>()");
        thenStatement.addStatement(mapStatement);
        for (CtField<?> field : fieldList) {
            thenStatement.addStatement(helper.createFormatCodeSnippet("map123456678.put(\"%s\", this.%s)", field.getSimpleName(), field.getSimpleName()));
        }
        CtStatement invocationStatement = helper.createFormatCodeSnippet(
                "com.lexcorp.joura.listeners.FiledChangeListener.getInstance().accept(this, this.getClass(), \"%s\", map123456678)",
                method.getSimpleName()
        );
        thenStatement.addStatement(invocationStatement);
        return helper.createCtIf(condition, thenStatement, null);
    }

    public static CtMethod<Void> createStartTrackMethod(Factory factory) {
        return createTrackMethod(factory, "startTrack");

    }

    public static CtMethod<Void> createStopTrackMethod(Factory factory) {
        return createTrackMethod(factory, "stopTrack");
    }


    public static CtStatement createStartTrackMethodBody(Factory factory, CtClass<?> ctClass, CtFieldReference<Boolean> trackFiledReference) {
        return createTrackMethodBody(factory, ctClass, trackFiledReference, true);
    }

    public static CtStatement createStopTrackMethodBody(Factory factory, CtClass<?> ctClass, CtFieldReference<Boolean> trackFiledReference) {
        return createTrackMethodBody(factory, ctClass, trackFiledReference, false);
    }

    private static CtMethod<Void> createTrackMethod(Factory factory, String methodName) {
        CtMethod<Void> trackMethod = factory.Core().createMethod();
        trackMethod.setSimpleName(methodName);
        trackMethod.setType(factory.Type().VOID_PRIMITIVE);
        trackMethod.setModifiers(Collections.singleton(ModifierKind.PUBLIC));
        trackMethod.setBody(factory.createBlock());
        return trackMethod;
    }

    private static CtStatement createTrackMethodBody(Factory factory, CtClass<?> ctClass, CtFieldReference<Boolean> trackFiledReference, boolean value) {
        CtHelper helper = new CtHelper(factory);
        CtFieldWrite<Boolean> ctFieldWrite = helper.createCtFieldWrite(factory.createThisAccess(ctClass.getTypeErasure()), trackFiledReference);
        CtLiteral<Boolean> ctLiteral = helper.createCtLiteral(value, factory.Type().BOOLEAN);
        return helper.createCtAssignment(ctFieldWrite, ctLiteral, factory.Type().BOOLEAN);
    }

}
