package com.lexcorp.joura.compile.analysis.alias;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.lexcorp.joura.compile.processors.TrackProcessor;
import com.lexcorp.joura.runtime.Trackable;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import static com.lexcorp.joura.compile.analysis.alias.Instance.Type;

public class AliasAnalysis {
    private final Logger logger = Logger.getLogger(AliasAnalysis.class.getName());

    private final CtClass<?> ctClass;
    public final Aliases fieldAliases = new Aliases();
    private final HashMap<String, Set<Instance>> methodReturns;
    private final HashMap<CtMethod<?>, Aliases> methodAliases;


    public AliasAnalysis(CtClass<?> ctClass) {
        this.ctClass = ctClass;
        this.methodReturns = new HashMap<>();
        this.methodAliases = new HashMap<>();
    }

    public boolean isTrackable(CtParameter<?> ctParameter) {
        return this.isTrackable(ctParameter.getType());
    }

    public boolean isTrackable(CtTypeReference<?> typeReference) {
        Factory factory = ctClass.getFactory();
        CtClass<?> ctClass = factory.Class()
                .get(typeReference.getQualifiedName());
        if (ctClass == null) {
            return false;
        }
        return ctClass.getSuperInterfaces().contains(factory.createCtTypeReference(Trackable.class));
    }

    public void run() {
        List<CtMethod<?>> methods = ctClass.getAllMethods().stream()
                .filter(m -> isTrackable(m.getType()))
                .collect(Collectors.toList());
        for (CtMethod<?> method : ctClass.getAllMethods()) {
            String methodSignature = getMethodSignature(method);
            methodReturns.put(methodSignature, new HashSet<>());
            Aliases aliases = runForMethod(method);
            methodAliases.put(method, aliases);
            aliases.getMap().forEach((name, alias) -> {
                if (name.startsWith("this")) {
                    fieldAliases.addWithMerge(name, alias);
                }
            });
        }
    }

    public Aliases runForMethod(CtMethod<?> method) {
        Aliases aliases = new Aliases();

        method.getParameters().stream()
                .filter(this::isTrackable)
                .forEach(p -> aliases.add(p.getSimpleName(), aliases.obj(Type.UNKNOWN)));

        List<CtAssignment<?, ?>> assignVariableStatements = method.getElements(Objects::nonNull);
        assignVariableStatements.stream()
                .filter(ctAssignment -> isTrackable(ctAssignment.getType()))
                .filter(ctAssignment -> ctAssignment.getAssigned() instanceof CtVariableWrite)
                .forEach( ctAssignment -> checkValidAssignment(aliases, ctAssignment));

        List<CtLocalVariable<?>> createLocalVariableStatements = method.getElements(Objects::nonNull);
        createLocalVariableStatements.stream()
                .filter(ctAssignment -> isTrackable(ctAssignment.getType()))
                .forEach(ctLocalVariable -> createLocalVariableAnalysis(aliases, ctLocalVariable));

        return aliases;
    }

    public Aliases method(CtMethod<?> method) {
        return methodAliases.get(method);
    }

    private void checkValidAssignment(Aliases aliases, CtAssignment<?, ?> ctAssignment) {
        CtExpression<?> assignment = ctAssignment.getAssignment();
        Alias assignedAlias = aliases.get(ctAssignment.getAssigned().toString());
        if (assignment instanceof CtVariableRead) {
            Alias assignmentAlias = aliases.get(assignment.toString());
            assignedAlias.merge(assignmentAlias);
        } else if (assignment instanceof CtThisAccess) {
            aliases.addThisAlias(assignedAlias);
        } else if (assignment instanceof CtConstructorCall) {
            aliases.addNewObjAlias(assignedAlias);
        } else if (assignment instanceof CtInvocation) {
            CtInvocation<?> ctInvocation = (CtInvocation<?>) ctAssignment.getAssignment();
            String methodSignature = getMethodSignature(ctInvocation.getExecutable().getDeclaration());
            if (methodReturns.containsKey(methodSignature)) {
                if (methodReturns.get(methodSignature).isEmpty()) {
                    aliases.add(assignedAlias, aliases.obj(Type.UNKNOWN));
                } else {
                    aliases.add(assignedAlias, methodReturns.get(methodSignature));
                }
            }
        }
    }

    private void createLocalVariableAnalysis(Aliases aliases, CtLocalVariable<?> ctLocalVariable) {
        String aliasName = ctLocalVariable.getSimpleName();
        CtExpression<?> ctExpression = ctLocalVariable.getDefaultExpression();
        if (ctExpression instanceof CtThisAccess) {
            aliases.addThisAlias(aliasName);
        }
        if (ctExpression instanceof CtVariableRead) {
            if (aliases.contains(ctExpression.toString())) {
                aliases.addWithMerge(aliasName, ctExpression.toString());
            }
        }
        if (ctExpression instanceof CtConstructorCall) {
            aliases.addNewObjAlias(aliasName);
        }
    }

    private String getMethodSignature(CtExecutable<?> ctExecutable) {
        return ctExecutable.getType().toString() + '.' + ctExecutable.getSignature();
    }
}
