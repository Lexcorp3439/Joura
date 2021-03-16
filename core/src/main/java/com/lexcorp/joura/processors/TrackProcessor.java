package com.lexcorp.joura.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.lexcorp.joura.Trackable;
import com.lexcorp.joura.utils.TrackCodeBuilder;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import static com.lexcorp.joura.utils.TrackCodeBuilder.buildTrackExpression;

public class TrackProcessor extends AbstractProcessor<CtClass<?>> {
    private final Logger log = Logger.getLogger(TrackProcessor.class.getName());

    @Override
    public boolean isToBeProcessed(CtClass<?> ctClass) {
        final CtTypeReference<?> trackableRef = getFactory().createCtTypeReference(Trackable.class);
        return ctClass.getSuperInterfaces().contains(trackableRef);
    }

    @Override
    public void process(CtClass<?> ctClass) {
        log.info("START");
        Set<CtMethod<?>> methods = ctClass.getAllMethods();
        Set<CtMethod<?>> classMethods = ctClass.getMethods();
        List<CtField<?>> classFields = ctClass.getFields();
        CtField<Boolean> trackField = createTrackField();

        ctClass.addField(0, trackField);
        List<CtMethod<?>> startTrackMethods = classMethods.stream().filter(ctMethod -> ctMethod.getSimpleName().equals("startTrack")).collect(Collectors.toList());
        List<CtMethod<?>> stopTrackMethods = classMethods.stream().filter(ctMethod -> ctMethod.getSimpleName().equals("stopTrack")).collect(Collectors.toList());

        if (startTrackMethods.size() == 0) {
            ctClass.addMethod(TrackCodeBuilder.createStartTrackMethod(getFactory()));
        }
        if (stopTrackMethods.size() == 0) {
            ctClass.addMethod(TrackCodeBuilder.createStopTrackMethod(getFactory()));
        }
        ctClass.getMethod("startTrack").getBody().insertEnd(
                TrackCodeBuilder.createStartTrackMethodBody(getFactory(), ctClass, trackField.getReference())
        );
        ctClass.getMethod("stopTrack").getBody().insertEnd(
                TrackCodeBuilder.createStopTrackMethodBody(getFactory(), ctClass, trackField.getReference())
        );

        for (CtMethod<?> method : methods) {
            if (method.getSimpleName().equals("setValue4")) {
                System.out.println("J");
            }
            List<CtField<?>> editableFields = getEditableFieldsFromMethod(method, classFields);
            if (editableFields.size() != 0) {
                CtStatement statement = TrackCodeBuilder.buildTrackExpression(getFactory(), ctClass, trackField.getReference(), method, editableFields);

                if (method.getType().equals(getFactory().Type().VOID_PRIMITIVE)) {
                    method.getBody().insertEnd(statement);
                } else {
                    List<CtReturn<?>> returnElements = method.getElements(Objects::nonNull);
                    returnElements.forEach(ctReturn -> {
                        if (ctReturn.getElements(e -> e instanceof CtInvocation).size() > 0) {
                            method.getBody().removeStatement(ctReturn);
                            CtLocalVariable<?> localVariableStatement = getFactory().createLocalVariable(
                                    (CtTypeReference) method.getType(), "__returnVar", ctReturn.getReturnedExpression()
                            );
                            CtReturn<?> ctReturnStatement = getFactory().createReturn().setReturnedExpression(
                                    (CtVariableRead) getFactory().createVariableRead(localVariableStatement.getReference(), false)
                            );
                            method.getBody().insertEnd(localVariableStatement);
                            method.getBody().insertEnd(statement);
                            method.getBody().insertEnd(ctReturnStatement);
                        }
                    });
                }
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
        editableFields.retainAll(fields);
        return new ArrayList<>(editableFields);
    }

    private CtField<Boolean> createTrackField() {
        Set<ModifierKind> modifierKinds = Collections.singleton(ModifierKind.PRIVATE);

        CtCodeSnippetExpression<Boolean> snippet1 = getFactory().Core().createCodeSnippetExpression();
        snippet1.setType(getFactory().Type().BOOLEAN);
        snippet1.setValue("false");

        CtTypeReference<Boolean> reference = getFactory().Type().BOOLEAN;
        CtType<Boolean> type = reference.getTypeDeclaration();
        CtField<?> field = (CtField<?>) getFactory().createField(type, modifierKinds, reference, "NEED_TRACK", snippet1);
        field.setDocComment("Tracking flag");
        return (CtField<Boolean>) field;
    }


    @Override
    public void processingDone() {
        log.info("DONE");
    }

}
