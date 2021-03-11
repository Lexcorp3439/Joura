package com.lexcorp.joura.listeners;

import java.util.List;
import java.util.logging.Logger;

import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

public class FiledChangeListener {
    private final Logger log = Logger.getLogger(FiledChangeListener.class.getName());
    private static final FiledChangeListener INSTANCE = new FiledChangeListener();

    public static FiledChangeListener getInstance() {
        return INSTANCE;
    }

    private FiledChangeListener() {
    }

    public void accept(CtMethod<?> method, List<CtField<?>> fields) {
        log.info(method.getSimpleName());
        log.info(fields.toString());
    }

}
