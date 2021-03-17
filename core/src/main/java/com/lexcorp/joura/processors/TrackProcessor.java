package com.lexcorp.joura.processors;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.lexcorp.joura.TrackOptions;
import com.lexcorp.joura.Trackable;
import com.lexcorp.joura.utils.Steps;
import com.lexcorp.joura.utils.Strategy;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

public class TrackProcessor extends AbstractProcessor<CtClass<?>> {
    private final Logger log = Logger.getLogger(TrackProcessor.class.getName());
    private Steps steps;

    @Override
    public boolean isToBeProcessed(CtClass<?> ctClass) {
        final CtTypeReference<?> trackableRef = getFactory().createCtTypeReference(Trackable.class);
        return ctClass.getSuperInterfaces().contains(trackableRef);
    }

    @Override
    public void process(CtClass<?> ctClass) {
        steps = new Steps(getFactory(), ctClass);
        log.info("START");

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
        log.info("DONE");
    }

}
