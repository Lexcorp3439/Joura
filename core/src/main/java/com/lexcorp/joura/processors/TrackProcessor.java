package com.lexcorp.joura.processors;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.lexcorp.joura.Trackable;
import com.lexcorp.joura.analysis.Steps;
import com.lexcorp.joura.options.Assign;
import com.lexcorp.joura.options.Strategy;
import com.lexcorp.joura.options.TrackOptions;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

public class TrackProcessor extends AbstractProcessor<CtClass<? extends Trackable>> {
    private final Logger log = Logger.getLogger(TrackProcessor.class.getName());
    private Steps steps;

    @Override
    public void process(CtClass<? extends Trackable> ctClass) {
        steps = new Steps(getFactory(), ctClass);

        TrackOptions trackOptions = ctClass.getAnnotation(TrackOptions.class);
        boolean alwaysTrack = trackOptions != null && trackOptions.alwaysTrack();
        Strategy analysingStrategy = trackOptions == null ? Strategy.DEFAULT : trackOptions.analysingStrategy();


        CtField<Boolean> trackField = steps.createClassTrackFieldIfNotAssigned(alwaysTrack);
        ctClass.addField(0, trackField);

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
            if (method.getSimpleName().equals("invokeMethod")) {
                System.out.println();
            }
            Collection<CtField<?>> editableFields = method.hasAnnotation(Assign.class)
                    ? steps.getAssignedFields(method)
                    : steps.analyser(analysingStrategy).getEditableFieldsFromMethod(method);

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
