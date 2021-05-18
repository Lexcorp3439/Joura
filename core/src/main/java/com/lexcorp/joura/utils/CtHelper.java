package com.lexcorp.joura.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lexcorp.joura.logger.JouraLogger;
import com.lexcorp.joura.logger.Marker;
import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.handlers.LogEventHandler;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

import static com.lexcorp.joura.utils.StringHelper.createFieldName;

public class CtHelper {
    private static final JouraLogger logger = JouraLogger.get(LogEventHandler.class);

    Factory factory;

    public CtHelper(Factory factory) {
        this.factory = factory;
    }

    public static boolean isTrackable(CtTypeReference<?> typeReference) {
        Factory factory = typeReference.getFactory();
        CtClass<?> ctClass = factory.Class().get(typeReference.getQualifiedName());
        return isTrackable(ctClass);
    }

    public static boolean isTrackable(CtClass<?> ctClass) {
        Factory factory = ctClass.getFactory();
        CtTypeReference<?> trackableType = factory.createCtTypeReference(Trackable.class);
        CtClass<?> parentClass;
        CtTypeReference<?> parentType = ctClass.getTypeErasure();
        System.out.println("=================================");
        while (parentType != null) {
            parentClass = factory.Class().get(parentType.getQualifiedName());
            if (parentClass == null) {
                logger.warn(
                        Marker.getMarker("CT_HELPER"),
                        "Could not find CtClass for type: " + parentType.getQualifiedName()
                );
                return false;
            }
            if (parentClass.getSuperInterfaces().contains(trackableType)) {
                return true;
            }
            parentType = parentClass.getSuperclass();
        }
        return false;
    }

    public static Set<CtTypeReference<?>> getExtendsTypesSet(CtClass<?> ctClass) {
        Set<CtTypeReference<?>> extendsTypes = new HashSet<>();
        Factory factory = ctClass.getFactory();
        extendsTypes.add(ctClass.getTypeErasure());
        CtTypeReference<?> parentType = ctClass.getSuperclass();
        while (parentType != null) {
            extendsTypes.add(parentType);
            CtClass<?> parentClass = factory.Class().get(parentType.getQualifiedName());
            parentType = parentClass.getSuperclass();
        }
        return extendsTypes;
    }

    public <T> CtField<T> createCtField(String filedName, CtTypeReference<T> typeReference, CtExpression<T> expression) {
        Set<ModifierKind> modifierKinds = Collections.singleton(ModifierKind.PRIVATE);
        CtField<T> field = factory.createField();
        field.setType(typeReference);
        field.setModifiers(modifierKinds);
        field.setSimpleName(filedName);
        field.setDefaultExpression(expression);
        return field;
    }

    public <T> CtLocalVariable<T> createLocalVar(CtTypeReference<T> typeReference, CtExpression<T> expression) {
        return createLocalVar(createFieldName(), typeReference, expression);
    }

    public <T> CtLocalVariable<T> createLocalVar(String filedName, CtTypeReference<T> typeReference, CtExpression<T> expression) {
        CtLocalVariable<T> localVariable = factory.createLocalVariable();
        localVariable.setType(typeReference);
        localVariable.setSimpleName(filedName);
        localVariable.setDefaultExpression(expression);
        return localVariable;
    }

    public <T> CtMethod<T> createMethod(String methodName, CtTypeReference<T> typeReference, Set<ModifierKind> modifiers) {
        CtMethod<T> method = factory.Core().createMethod();
        method.setSimpleName(methodName);
        method.setType(typeReference);
        method.setModifiers(modifiers);
        method.setBody(factory.createBlock());
        return method;
    }

    public <T> CtMethod<T> createMethodWithParameters(
            String methodName, CtTypeReference<T> typeReference, Set<ModifierKind> modifiers, List<CtParameter<?>> parameters
    ) {
        CtMethod<T> method = factory.Core().createMethod();
        method.setSimpleName(methodName);
        method.setType(typeReference);
        method.setParameters(parameters);
        method.setModifiers(modifiers);
        method.setBody(factory.createBlock());
        return method;
    }

    public <T> CtParameter<T> createCtParameter(CtTypeReference<T> type, String name) {
        CtParameter<T> ctParameter = factory.createParameter();
        ctParameter.setType(type);
        ctParameter.setSimpleName(name);
        return ctParameter;
    }

    public <T> CtLiteral<T> createCtLiteral(T value, CtTypeReference<T> type) {
        CtLiteral<T> ctLiteral = factory.createLiteral();
        ctLiteral.setValue(value);
        ctLiteral.setType(type);
        return ctLiteral;
    }

    public CtThisAccess<?> createThisAccess(CtClass<?> ctClass) {
        return factory.createThisAccess(ctClass.getTypeErasure());
    }

    public <T> CtFieldRead<T> createCtFieldRead(CtClass<?> ctClass, CtField<T> varable) {
        CtFieldRead<T> ctFieldRead = factory.createFieldRead();
        ctFieldRead.setTarget(this.createThisAccess(ctClass));
        ctFieldRead.setVariable(varable.getReference());
        return ctFieldRead;
    }

    public <T> CtFieldRead<T> createCtFieldRead(CtExpression<?> target, CtVariableReference<T> varable) {
        CtFieldRead<T> ctFieldRead = factory.createFieldRead();
        ctFieldRead.setTarget(target);
        ctFieldRead.setVariable(varable);
        return ctFieldRead;
    }

    public <T> CtVariableRead<T> createCtVariableRead(CtVariableReference<T> variableReference, boolean implicit) {
        CtVariableRead<T> ctVariableRead = factory.createVariableRead();
        ctVariableRead.setVariable(variableReference);
        ctVariableRead.setImplicit(implicit);
        return ctVariableRead;
    }

    public <T> CtFieldWrite<T> createCtFieldWrite(CtExpression<?> target, CtVariableReference<T> varable) {
        CtFieldWrite<T> ctFieldWrite = factory.createFieldWrite();
        ctFieldWrite.setTarget(target);
        ctFieldWrite.setVariable(varable);
        return ctFieldWrite;
    }

    public <T, A extends T> CtAssignment<T, A> createCtAssignment(CtExpression<T> assigned, CtExpression<A> assignment, CtTypeReference<T> type) {
        CtAssignment<T, A> ctAssignment = factory.createAssignment();
        ctAssignment.setAssigned(assigned);
        ctAssignment.setAssignment(assignment);
        ctAssignment.setType(type);
        return ctAssignment;
    }

//    public CtStatement createCtStatement() {
//        factory.createExecutableReference().s
//        factory.createInvocation()
//    }

    public <T> CtReturn<T> createCtReturn(CtExpression<T> ctExpression) {
        CtReturn<T> ctReturn = factory.createReturn();
        ctReturn.setReturnedExpression(ctExpression);
        return ctReturn;
    }

    public CtIf createCtIf(CtExpression<Boolean> condition, CtStatement thenStatement, CtStatement elseStatement) {
        CtIf ctIf = factory.createIf();
        ctIf.setCondition(condition);
        ctIf.setThenStatement(thenStatement);
        ctIf.setElseStatement(elseStatement);
        return ctIf;
    }

    public CtStatement createFormatCodeSnippet(String code, String... variables) {
        CtCodeSnippetStatement snippet = factory.createCodeSnippetStatement();
        String codeSnippet = String.format(code, variables);
        snippet.setValue(codeSnippet);
        return snippet;
    }

    private CtCodeSnippetStatement createSimpleCodeSnippet(String code) {
        CtCodeSnippetStatement snippet = factory.Core().createCodeSnippetStatement();
        snippet.setValue(code);
        return snippet;
    }

}
