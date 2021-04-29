package com.lexcorp.joura.analysis.point;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class AliasAnalysis {
    private final CtClass<?> ctClass;
    private final Aliases aliases = new Aliases();

    public AliasAnalysis(CtClass<?> ctClass) {
        this.ctClass = ctClass;
    }

    public void preAnalysis() {
        for (CtMethod<?> method : ctClass.getMethods()) {

        }

    }
}
