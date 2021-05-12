package com.lexcorp.joura.compile.processors;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.lexcorp.joura.compile.analysis.Steps;
import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.options.Assign;
import com.lexcorp.joura.runtime.options.Strategy;
import com.lexcorp.joura.runtime.options.TrackOptions;

import com.lexcorp.joura.tests.ExpectedFields;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

public class TrackProcessor extends AbstractProcessor<CtClass<? extends Trackable>> {
    private final Logger log = Logger.getLogger(TrackProcessor.class.getName());
    private Steps steps;

    @Override
    public boolean isToBeProcessed(CtClass<? extends Trackable> candidate) {
        return candidate.getSuperInterfaces().contains(getFactory().createCtTypeReference(Trackable.class));
    }

    @Override
    public void process(CtClass<? extends Trackable> ctClass) {
        System.out.println("RUN FOR CLASS " + ctClass.getSimpleName());
        steps = new Steps(getFactory(), ctClass);

        TrackOptions trackOptions = ctClass.getAnnotation(TrackOptions.class);
        boolean alwaysTrack = trackOptions != null && trackOptions.alwaysTrack();
        Strategy analysingStrategy = trackOptions == null ? Strategy.DEFAULT : trackOptions.analysingStrategy();


        CtField<Boolean> trackField = steps.createClassTrackFieldIfNotAssigned(alwaysTrack);
        ctClass.addField(0, trackField);

        CtField<String> identifierField = steps.createIdentifierField();
        ctClass.addField(1, identifierField);

        CtMethod<Void> setTagMethod = steps.createsetTagMethod();
        ctClass.addMethod(setTagMethod);

        CtMethod<String> getTagMethod = steps.creategetTagMethod();
        ctClass.addMethod(getTagMethod);

        if (!alwaysTrack) {
            CtMethod<?> startTrack = steps.createTrackMethodIfNotExist(true);
            CtMethod<?> stopTrack = steps.createTrackMethodIfNotExist(false);
            if (startTrack.equals(stopTrack)) {
                steps.updateMethodWithStatement(startTrack, steps.createTrackMethodBodyWithInvert());
            } else {
                steps.updateMethodWithStatement(startTrack, steps.createTrackMethodBody(true));
                steps.updateMethodWithStatement(stopTrack, steps.createTrackMethodBody(false));
            }
        }

        List<CtMethod<?>> methods = steps.getTrackedMethods();

        for (CtMethod<?> method : methods) {
            if (method.getSimpleName().equals("testAssignWithThis")) {
                System.out.println();
            }

            Collection<CtField<?>> editableFields = method.hasAnnotation(Assign.class)
                    ? steps.getAssignedFields(method)
                    : steps.analyser(analysingStrategy).getEditableFieldsFromMethod(method);

            if (method.hasAnnotation(ExpectedFields.class)) {
                Set<String> actualFields = editableFields.stream()
                        .map(CtField::getSimpleName)
                        .collect(Collectors.toSet());
                Set<String> expectedFields = new java.util.HashSet<>(
                        Set.of(method.getAnnotation(ExpectedFields.class).fields())
                );
                expectedFields.removeAll(actualFields);
                assert expectedFields.size() == 0: "In method " + method.getSignature() +
                        " lost expected fields " + expectedFields.toString() +
                        " actual fields " + actualFields.toString();
            }

            if (editableFields.size() != 0) {
                CtStatement fieldChangeNotifierStatement = steps.createFieldChangeNotifierStatement(
                        trackField.getReference(), method, editableFields
                );

                steps.updateMethodWithStatement(method, fieldChangeNotifierStatement);
            }
        }
    }


    @Override
    public void processingDone() {
        // TODO: log process stat
    }

}
