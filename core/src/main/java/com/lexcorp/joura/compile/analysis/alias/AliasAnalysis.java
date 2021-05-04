package com.lexcorp.joura.compile.analysis.alias;

import java.util.List;
import java.util.Objects;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import static com.lexcorp.joura.compile.analysis.alias.Instance.Type;

public class AliasAnalysis {
    private final CtClass<?> ctClass;
    private final Aliases aliases = new Aliases();

    public AliasAnalysis(CtClass<?> ctClass) {
        this.ctClass = ctClass;
    }

    public void preAnalysis() {
//        ctClass.getAllMethods().stream().filter(m -> m.getType().equals())
        for (CtMethod<?> method : ctClass.getMethods()) {

        }

    }

    public void runForMethod(CtMethod<?> method) {
        method.getParameters().forEach(p -> {
            if (p.getType().equals(ctClass.getTypeErasure())) {
                aliases.add(p.getSimpleName(), aliases.obj(Type.UNKNOWN));
            }
        });

        List<CtAssignment<?, ?>> assignVariableStatements = method.getElements(Objects::nonNull);
        assignVariableStatements.stream()
                .filter(ctAssignment -> ctAssignment.getType().equals(ctClass.getTypeErasure()))
                .filter(ctAssignment -> ctAssignment.getAssigned() instanceof CtVariableWrite)
                .forEach(this::checkValidAssignment);

        List<CtLocalVariable<?>> createLocalVariableStatements = method.getElements(Objects::nonNull);
        createLocalVariableStatements.forEach(this::createLocalVariableAnalysis);

    }

    private void checkValidAssignment(CtAssignment<?, ?> ctAssignment) {
        CtExpression<?> assignment = ctAssignment.getAssignment();
        Alias assignedAlias = aliases.get(ctAssignment.getAssigned().toString());
        if (assignment instanceof CtVariableRead) {
            Alias assignmentAlias = aliases.get(assignment.toString());
            assignedAlias.merge(assignmentAlias);
        } else if (assignment instanceof CtThisAccess) {
            aliases.addThisAlias(assignedAlias);
        } else if (assignment instanceof CtConstructorCall) {
            aliases.addNewObjAlias(assignedAlias);
        }
//        else {
//            throw new RuntimeException("Illegal state");
//        }
    }

    public boolean isThisAlias(String variableName) {
        return aliases.get(variableName).containsThis();
    }

    private void createLocalVariableAnalysis(CtLocalVariable<?> ctLocalVariable) {
        CtExpression<?> ctExpression = ctLocalVariable.getDefaultExpression();
        if (ctExpression instanceof CtThisAccess) {
            aliases.addThisAlias(ctLocalVariable.getSimpleName());
        }
        if (ctExpression instanceof CtVariableRead) {
//            variables.containsKey(((CtVariableRead<?>) ctExpression).getVariable().getSimpleName())
            if (true) {
//                aliases.add();
//                Point point = new Point(ctLocalVariable, iterCount);
//                variables.put(ctLocalVariable.getSimpleName(), point);
//                pointRelationship.put(point, thisPoint);
            }

        }
        if (ctExpression instanceof CtConstructorCall) {
            aliases.addNewObjAlias(ctLocalVariable.getSimpleName());
        }
    }

}
