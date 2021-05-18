package com.lexcorp.joura.compile.analysis.alias;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.lexcorp.joura.logger.JouraLogger;
import com.lexcorp.joura.runtime.Trackable;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import static com.lexcorp.joura.compile.analysis.alias.Instance.Type;
import static com.lexcorp.joura.logger.Markers.Compile.CREATE_LOCAL_VAR_ANALYSIS;
import static com.lexcorp.joura.logger.Markers.Compile.END_ALIAS_METHOD_MARKER;
import static com.lexcorp.joura.logger.Markers.Compile.ITER_ALIAS_METHOD_MARKER_END;
import static com.lexcorp.joura.logger.Markers.Compile.ITER_ALIAS_METHOD_MARKER_START;
import static com.lexcorp.joura.logger.Markers.Compile.START_ALIAS_METHOD_MARKER;

public class AliasAnalyser {
    private static final JouraLogger logger = JouraLogger.get(AliasAnalyser.class);

    private final CtClass<?> ctClass;
    public final Aliases fieldAliases = new Aliases();
    private final HashMap<String, Set<Instance>> methodReturns;
    private final HashMap<CtMethod<?>, Aliases> methodAliases;


    public AliasAnalyser(CtClass<?> ctClass) {
        this.ctClass = ctClass;
        this.methodReturns = new HashMap<>();
        this.methodAliases = new HashMap<>();
    }

    public boolean isTrackable(CtParameter<?> ctParameter) {
        return this.isTrackable(ctParameter.getType());
    }

    public boolean isTrackable(CtTypeReference<?> typeReference) {
        Factory factory = this.ctClass.getFactory();
        CtClass<?> ctClass = factory.Class()
                .get(typeReference.getQualifiedName());
        if (ctClass == null) {
            return false;
        }
        return ctClass.getSuperInterfaces().contains(factory.createCtTypeReference(Trackable.class));
    }

    public void run() {
        for (CtMethod<?> method : this.ctClass.getAllMethods()) {
            String methodSignature = getMethodSignature(method);
            if (isTrackable(method.getType())) {
                methodReturns.put(methodSignature, new HashSet<>());
            }
            Aliases aliases = runForMethod(method);
            methodAliases.put(method, aliases);
            aliases.getMap().forEach((name, alias) -> {
                if (name.startsWith("this")) {
                    fieldAliases.merge(name, alias);
                }
            });
        }
    }

    public Aliases runForMethod(CtMethod<?> method) {
        Aliases aliases = new Aliases();
        boolean is_changed = true;
        logger.info(START_ALIAS_METHOD_MARKER, method.getSignature());
        method.getParameters().stream()
                .filter(this::isTrackable)
                .forEach(p -> aliases.add(p.getSimpleName(), aliases.obj(Type.UNKNOWN)));

        for (int i = 0; is_changed; i++) {
            logger.info(ITER_ALIAS_METHOD_MARKER_START, String.valueOf(i));
            List<CtLocalVariable<?>> createLocalVariableStatements = method.getElements(Objects::nonNull);
            //noinspection SimplifyStreamApiCallChains
            boolean isChangedWithLocalVarStatements = createLocalVariableStatements.stream()
                    .filter(ctAssignment -> isTrackable(ctAssignment.getType()))
                    .map(ctLocalVariable -> createLocalVariableAnalysis(aliases, ctLocalVariable))
                    .anyMatch(r -> r);

            List<CtAssignment<?, ?>> assignVariableStatements = method.getElements(Objects::nonNull);
            //noinspection SimplifyStreamApiCallChains
            boolean isChangedWithAssignVarStatements = assignVariableStatements.stream()
                    .filter(ctAssignment -> isTrackable(ctAssignment.getType()))
                    .filter(ctAssignment -> ctAssignment.getAssigned() instanceof CtVariableWrite)
                    .map(ctAssignment -> checkValidAssignment(aliases, ctAssignment))
                    .anyMatch(r -> r);
            logger.info(
                    ITER_ALIAS_METHOD_MARKER_END,
                    "create_local_var=" + isChangedWithLocalVarStatements + " " +
                            "assign_local_var=" + isChangedWithAssignVarStatements
            );
            is_changed = isChangedWithAssignVarStatements || isChangedWithLocalVarStatements;
        }
        logger.info(END_ALIAS_METHOD_MARKER, aliases.toString() + "\n" + "===============================");
        return aliases;
    }

    public Aliases method(CtMethod<?> method) {
        return methodAliases.get(method);
    }

    private boolean checkValidAssignment(Aliases aliases, CtAssignment<?, ?> ctAssignment) {
        logger.info(CREATE_LOCAL_VAR_ANALYSIS, ctAssignment.toString());
        CtExpression<?> assignment = ctAssignment.getAssignment();
        Alias assignedAlias = aliases.get(ctAssignment.getAssigned().toString());
        return this.checkExpression(aliases, assignedAlias, assignment);
    }

    private boolean createLocalVariableAnalysis(Aliases aliases, CtLocalVariable<?> ctLocalVariable) {
        logger.info(CREATE_LOCAL_VAR_ANALYSIS, ctLocalVariable.toString());
        Alias alias = aliases.get(ctLocalVariable.getSimpleName());
        CtExpression<?> ctExpression = ctLocalVariable.getDefaultExpression();
        return this.checkExpression(aliases, alias, ctExpression);
    }

    private boolean checkExpression(Aliases aliases, Alias alias, CtExpression<?> ctExpression) {
        if (ctExpression instanceof CtThisAccess) {
            return aliases.addThisAlias(alias);
        }
        if (ctExpression instanceof CtVariableRead) {
            if (aliases.contains(ctExpression.toString())) {
                return aliases.merge(alias, ctExpression.toString());
            }
        }
        if (ctExpression instanceof CtConstructorCall) {
            return aliases.addNewObjAlias(alias);
        }
        if (ctExpression instanceof CtInvocation) {
            CtInvocation<?> ctInvocation = (CtInvocation<?>) ctExpression;
            String methodSignature = getMethodSignature(ctInvocation.getExecutable().getDeclaration());
            if (methodReturns.containsKey(methodSignature)) {
                return methodReturns.get(methodSignature).isEmpty()
                        ? aliases.add(alias, aliases.obj(Type.UNKNOWN))
                        : aliases.add(alias, methodReturns.get(methodSignature));
            }
        }
        return false;
    }


    private String getMethodSignature(CtExecutable<?> ctExecutable) {
        return ctExecutable.getType().toString() + '.' + ctExecutable.getSignature();
    }
}
