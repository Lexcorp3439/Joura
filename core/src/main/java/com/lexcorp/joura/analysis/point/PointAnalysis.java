package com.lexcorp.joura.analysis.point;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class PointAnalysis {
    private final HashMap<Point, Set<Point>> pointRelationship;
    private final CtMethod<?> method;
    private final CtClass<?> ctClass;
    private final HashMap<String, Point> variables;
    private final Iterator<CtStatement> iterator;
    private final Point thisPoint = new Point("this", 0);
    private final Point nullPoint = new Point("null", 0);
    private int iterCount = 0;

    public PointAnalysis(CtClass<?> ctClass, CtMethod<?> method) {
        this.method = method;
        this.ctClass = ctClass;
        this.iterator = method.getBody().iterator();
        pointRelationship = new HashMap<>();
        variables = new HashMap<>() {{
            put("this", thisPoint);
        }};
        method.getParameters().forEach(p -> {
            if (p.getType().equals(ctClass.getTypeErasure())) {
                Point point = new Point(p.getSimpleName(), 0);
                variables.put(p.getSimpleName(), point);
                pointRelationship.put(point, nullPoint);
            }
        });

    }

    public void run() {
        List<CtAssignment> list = method.getElements(Objects::nonNull);
        list.stream()
                .filter(ctAssignment -> ctAssignment.getType().equals(ctClass.getTypeErasure()))
                .filter(ctAssignment -> ctAssignment.getAssigned() instanceof CtVariableWrite)
                .forEach(this::checkValidAssignment);

        List<CtLocalVariable<?>> list1 = method.getElements(Objects::nonNull);
        list1.stream()
                .filter(ctAssignment -> ctAssignment.getType().equals(ctClass.getTypeErasure()))

        for (CtStatement statement : method.getBody()) {
            if (statement instanceof CtLocalVariable<?>) {
                createLocalVariableAnalysis((CtLocalVariable<?>) statement);
            }
        }
    }

    private void checkValidAssignment(CtAssignment<?, ?> ctAssignment) {
        CtExpression<?> assignment = ctAssignment.getAssignment();
        Point assignedPoint = variables.get(ctAssignment.getAssigned().toString());
        Point targetPoint = nullPoint;
        if (assignment instanceof CtVariableRead) {
            Point assignmentPoint = variables.get(assignment.toString());
            targetPoint = pointRelationship.get(assignmentPoint);
        } else if (assignment instanceof CtThisAccess) {
            targetPoint = thisPoint;
        } else if (!(assignment instanceof CtConstructorCall)) {
            throw new RuntimeException("Illegal state");
        }
        pointRelationship.put(assignedPoint, targetPoint);
    }

    public boolean isThisPointer(String variableName) {
        if (!variables.containsKey(variableName)) {
            return false;
        }
        return pointRelationship.get(variables.get(variableName)).equals(thisPoint);
    }

    private void createLocalVariableAnalysis(CtLocalVariable<?> ctLocalVariable) {
        CtExpression<?> ctExpression = ctLocalVariable.getDefaultExpression();
        if (ctExpression instanceof CtThisAccess) {
            Point point = new Point(ctLocalVariable, iterCount);
            variables.put(ctLocalVariable.getSimpleName(), point);
            pointRelationship.put(point, thisPoint);
        }
        if (ctExpression instanceof CtVariableRead &&
                variables.containsKey(((CtVariableRead<?>) ctExpression).getVariable().getSimpleName())) {
            Point point = new Point(ctLocalVariable, iterCount);
            variables.put(ctLocalVariable.getSimpleName(), point);
            pointRelationship.put(point, thisPoint);
        }
        if (ctExpression instanceof CtConstructorCall) {
            Point point = new Point(ctLocalVariable, iterCount);
            variables.put(ctLocalVariable.getSimpleName(), point);
            pointRelationship.put(point, nullPoint);
        }
    }
}
