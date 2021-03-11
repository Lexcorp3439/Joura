package com.lexcorp.joura.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import com.lexcorp.joura.Trackable1;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

public class TrackProcessor1 extends AbstractProcessor<CtClass<?>> {
    private final Logger log = Logger.getLogger(TrackProcessor1.class.getName());

    @Override
    public boolean isToBeProcessed(CtClass<?> ctClass) {
        final CtTypeReference<?> trackableRef = getFactory().createCtTypeReference(Trackable1.class);
        return ctClass.getSuperInterfaces().contains(trackableRef);
    }

    @Override
    public void process(CtClass<?> ctClass) {
        log.info("START");
        Set<CtMethod<?>> methods = ctClass.getAllMethods();
        List<CtField<?>> classFields = ctClass.getFields();

        ctClass.addField(0, createTrackField());
        ctClass.addMethod(createStartTrackMethod());
        ctClass.addMethod(createStopTrackMethod());

        for (CtMethod<?> method : methods) {
            List<CtField<?>> editableFields = getEditableFieldsFromMethod(method, classFields);
            if (editableFields.size() != 0) {
                if (method.getType().equals(getFactory().Type().voidType())) {
                    method.getBody().insertEnd(createSimpleCodeSnippet("if (this.NEED_TRACK) {System.out.println(\"Hello1111\");}"));
                } else {
                    List<CtReturn> returnElements = method.getElements(Objects::nonNull);
                    returnElements.forEach(ctReturn -> {
                        if (ctReturn.getElements(e -> e instanceof CtInvocation).size() > 0) {
                            method.getBody().removeStatement(ctReturn);
                            method.getBody().insertEnd(createFormatCodeSnippet(
                                    "%s __returnVar = %s",
                                    method.getType().toString(),
                                    ctReturn.getReturnedExpression().toString()
                            ));
                            method.getBody().insertEnd(createSimpleCodeSnippet("if (this.NEED_TRACK) {System.out.println(\"Hello1111\");}"));
                            method.getBody().insertEnd(createSimpleCodeSnippet("return __returnVar"));
                        }
                    });
                }
//                method.getBody().addStatement(
//                        method.getBody().getStatements().size() - 1,
//                        createSimpleCodeSnippet("if (this.NEED_TRACK) {System.out.println(\"Hello1111\");}")
//                );
            }
        }
    }

    private List<CtField<?>> getEditableFieldsFromMethod(CtMethod<?> method, List<CtField<?>> fields) {
        Set<CtField<?>> editableFields = new HashSet<>();
        method.getElements(e -> e instanceof CtInvocation).stream()
                .map(e -> e.getElements(element -> element instanceof CtFieldReference))
                .forEach(ctElementList -> {
                    ctElementList.forEach(ctElement ->
                            editableFields.add((((CtFieldRead<?>) ctElement.getParent()).getVariable()).getFieldDeclaration()));
                });
        method.getElements(e -> e instanceof CtFieldWrite).forEach(ctElement -> {
                    editableFields.add(((CtFieldWrite<?>) ctElement).getVariable().getFieldDeclaration());
                }
        );
        return new ArrayList<>(editableFields);
    }

    private CtField<?> createTrackField() {
        Set<ModifierKind> modifierKinds = Collections.singleton(ModifierKind.PRIVATE);

        CtCodeSnippetExpression<Boolean> snippet1 = getFactory().Core().createCodeSnippetExpression();
        snippet1.setType(getFactory().Type().BOOLEAN);
        snippet1.setValue("false");

        CtTypeReference<Boolean> reference = getFactory().Type().BOOLEAN;
        CtType<Boolean> type = reference.getTypeDeclaration();
        CtField<?> field = (CtField<?>) getFactory().createField(type, modifierKinds, reference, "NEED_TRACK", snippet1);
        field.setDocComment("Tracking flag");
        return field;
    }

    private CtMethod<?> createStartTrackMethod() {
        CtMethod startTrack = getFactory().Core().createMethod();
        startTrack.setSimpleName("startTrack");
        startTrack.setType(getFactory().Type().VOID_PRIMITIVE);
        startTrack.setModifiers(Collections.singleton(ModifierKind.PUBLIC));
        startTrack.setBody(createSimpleCodeSnippet("this.NEED_TRACK = true"));
        startTrack.getBody().insertEnd(createSimpleCodeSnippet("System.out.println(\"TRACKED\")"));
        return startTrack;
    }

    private CtMethod<?> createStopTrackMethod() {
        CtMethod startTrack = getFactory().Core().createMethod();
        startTrack.setSimpleName("stopTrack");
        startTrack.setType(getFactory().Type().VOID_PRIMITIVE);
        startTrack.setModifiers(Collections.singleton(ModifierKind.PUBLIC));
        startTrack.setBody(createSimpleCodeSnippet("this.NEED_TRACK = false"));
        startTrack.getBody().insertEnd(createSimpleCodeSnippet("System.out.println(\"UNTRACKED\")"));
        return startTrack;
    }

    private CtCodeSnippetStatement createSimpleCodeSnippet(String code) {
        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        snippet.setValue(code);
        return snippet;
    }

    private CtCodeSnippetStatement createFormatCodeSnippet(String code, String... variables) {
        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        String codeSnippet = String.format(code, variables);
        snippet.setValue(codeSnippet);
        return snippet;
    }

    @Override
    public void processingDone() {
        log.info("DONE");
    }

}
