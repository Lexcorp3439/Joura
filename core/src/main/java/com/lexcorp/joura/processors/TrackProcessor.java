package com.lexcorp.joura.processors;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.lexcorp.joura.Trackable;
import com.lexcorp.joura.analysis.Steps;
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


        CtField<Boolean> trackField = steps.createClassTrackField(alwaysTrack);
        ctClass.addField(0, trackField);

        if (!alwaysTrack) {
            CtMethod<Void> startTrack = steps.createTrackMethodIfNotExist(true);
            CtMethod<Void> stopTrack = steps.createTrackMethodIfNotExist(false);
            startTrack.getBody().insertEnd(steps.createTrackMethodBody(true, trackField.getReference()));
            stopTrack.getBody().insertEnd(steps.createTrackMethodBody(false, trackField.getReference()));
        }

        List<CtMethod<?>> methods = ctClass.getAllMethods().stream().filter(m->!m.isStatic()).collect(Collectors.toList());
        for (CtMethod<?> method : methods) {
            Collection<CtField<?>> editableFields = steps.analyser(analysingStrategy).getEditableFieldsFromMethod(method);
            if (editableFields.size() != 0) {
                CtStatement fieldChangeNotifierStatement = steps.createFieldChangeNotifierStatement(trackField.getReference(), method, editableFields);

                if (method.getType().equals(getFactory().Type().VOID_PRIMITIVE)) {
                    method.getBody().insertEnd(fieldChangeNotifierStatement);
                } else {
                    steps.updateReturnStatements(method, fieldChangeNotifierStatement);
                }
            }
        }
    }


    @Override
    public void processingDone() {
        // TODO: log process stat
    }

}
