package com.lexcorp.joura.compile.processors;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lexcorp.joura.compile.analysis.Steps;
import com.lexcorp.joura.logger.JouraLogger;
import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.handlers.LogEventHandler;
import com.lexcorp.joura.runtime.options.All;
import com.lexcorp.joura.runtime.options.Assign;
import com.lexcorp.joura.runtime.options.Strategy;
import com.lexcorp.joura.runtime.options.TrackOptions;
import com.lexcorp.joura.runtime.options.test.ExpectedFields;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

import static com.lexcorp.joura.logger.Markers.Compile.END_METHOD_PROCESSING_MARKER;
import static com.lexcorp.joura.logger.Markers.Compile.END_PROCESS_MARKER;
import static com.lexcorp.joura.logger.Markers.Compile.EXPECTED_FIELDS_MARKER;
import static com.lexcorp.joura.logger.Markers.Compile.RECEIVED_FIELDS_MARKER;
import static com.lexcorp.joura.logger.Markers.Compile.START_METHOD_PROCESSING_MARKER;
import static com.lexcorp.joura.logger.Markers.Compile.START_PROCESS_MARKER;

public class TrackProcessor extends AbstractProcessor<CtClass<? extends Trackable>> {
    private static final JouraLogger logger = JouraLogger.get(LogEventHandler.class);
    private Steps steps;

    @Override
    public boolean isToBeProcessed(CtClass<? extends Trackable> candidate) {
        return candidate.getSuperInterfaces().contains(getFactory().createCtTypeReference(Trackable.class));
    }

    @Override
    public void process(CtClass<? extends Trackable> ctClass) {
        logger.info(START_PROCESS_MARKER, ctClass.getSimpleName());
        steps = new Steps(getFactory(), ctClass);

        TrackOptions trackOptions = ctClass.getAnnotation(TrackOptions.class);
        Strategy analysingStrategy = trackOptions == null
                ? Strategy.ALIAS_ANALYSIS
                : trackOptions.strategy();
        steps.setUpAnalyser(analysingStrategy);
        steps.analyser().run();

        boolean alwaysTrack = analysingStrategy == Strategy.ALWAYS_TRACK;

        List<CtMethod<?>> methods = steps.getTrackedMethods();

        CtField<Boolean> trackField = steps.createClassTrackFieldIfNotAssigned(alwaysTrack);
        ctClass.addField(0, trackField);

        CtField<String> identifierField = steps.createTagField();
        ctClass.addField(1, identifierField);

        CtMethod<Void> setTagMethod = steps.createSetTagMethod();
        ctClass.addMethod(setTagMethod);

        CtMethod<String> getTagMethod = steps.createGetTagMethod();
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

        for (CtMethod<?> method : methods) {
            if (method.getSimpleName().equals("testAssignWithThis")) {
                System.out.println();
            }
            logger.info(START_METHOD_PROCESSING_MARKER, method.getSignature());

            Collection<CtField<?>> editableFields = method.hasAnnotation(All.class)
                    ? ctClass.getFields()
                    : method.hasAnnotation(Assign.class)
                    ? steps.getAssignedFields(method)
                    : steps.analyser().runForMethod(method);

            logger.info(RECEIVED_FIELDS_MARKER, editableFields.stream()
                    .map(CtField::getSimpleName)
                    .collect(Collectors.toSet()).toString());
            if (method.hasAnnotation(ExpectedFields.class)) {
                Set<String> actualFields = editableFields.stream()
                        .map(CtField::getSimpleName)
                        .collect(Collectors.toSet());
                Set<String> expectedFields = new java.util.HashSet<>(
                        Set.of(method.getAnnotation(ExpectedFields.class).fields())
                );
                logger.info(EXPECTED_FIELDS_MARKER, expectedFields.toString());
                expectedFields.removeAll(actualFields);
                assert expectedFields.size() == 0 : "In method " + method.getSignature() +
                        " lost expected fields " + expectedFields +
                        " actual fields " + actualFields;
            }

            if (editableFields.size() != 0) {
                CtStatement fieldChangeNotifierStatement = steps.createFieldChangeNotifierStatement(
                        trackField.getReference(), method, editableFields
                );

                steps.updateMethodWithStatement(method, fieldChangeNotifierStatement);
            }
            logger.info(END_METHOD_PROCESSING_MARKER, method.getSignature() + "\n");
        }
        logger.info(END_PROCESS_MARKER, ctClass.getSimpleName() + "\n");
    }


    @Override
    public void processingDone() {
        // TODO: log process stat
    }

}
