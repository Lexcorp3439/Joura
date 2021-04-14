package com.lexcorp.joura.analysis.point;

import java.util.HashMap;
import java.util.Iterator;

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
    private final HashMap<Point, Point> pointRelationship;
    private final CtMethod<?> method;
    private final CtClass<?> ctClass;
    private final HashMap<String, Point> variables;
    private final Iterator<CtStatement> iterator;
    private final Point thisPoint = new Point("this", 0);
    private final Point nullPoint = new Point("null", 0);
    private int iterCount = 0;

    public PointAnalysis(CtClass<?> ctClass, CtMethod<?> method) {
        System.out.println(ctClass.getSimpleName());
        System.out.println(method.getSimpleName());
        this.method = method;
        this.ctClass = ctClass;
        this.iterator = method.getBody().iterator();
        pointRelationship = new HashMap<>() {{
            put(thisPoint, nullPoint);
        }};
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

    public boolean nextIteration() {
        if (iterator.hasNext()) {
            iterCount++;
            CtStatement statement = iterator.next();
            if (statement instanceof CtLocalVariable<?>) {
                createLocalVariableAnalysis((CtLocalVariable<?>) statement);
            }
            if (statement instanceof CtAssignment) {
                checkAssignment((CtAssignment<?, ?>) statement);
            }

        }
        return iterator.hasNext();
    }

    private void checkAssignment(CtAssignment<?, ?> ctAssignment) {
        if (!ctAssignment.getType().equals(ctClass.getTypeErasure())) {
            return;
        }
        CtExpression<?> assigned = ctAssignment.getAssigned();
        CtExpression<?> assignment = ctAssignment.getAssignment();
        if (assigned instanceof CtVariableWrite) {
            String assignedName = ((CtVariableWrite<?>) assigned).getVariable().getSimpleName();
            Point assignedPoint = variables.get(assignedName);
            Point targetPoint = nullPoint;
            if (assignment instanceof CtVariableRead) {
                String assignmentName = ((CtVariableRead<?>) assignment).getVariable().getSimpleName();
                Point assignmentPoint = variables.get(assignmentName);
                targetPoint = pointRelationship.get(assignmentPoint);
            } else if (assignment instanceof CtThisAccess) {
                targetPoint = thisPoint;
            } else if (!(assignment instanceof CtConstructorCall)) {
                throw new RuntimeException("Illegal state");
            }
            pointRelationship.put(assignedPoint, targetPoint);

        }
    }

    public boolean isThisPointer(String variableName) {
        if (!variables.containsKey(variableName)) {
            return false;
        }
        return pointRelationship.get(variables.get(variableName)).equals(thisPoint);
    }

    private void createLocalVariableAnalysis(CtLocalVariable<?> ctLocalVariable) {
        if (!ctLocalVariable.getType().equals(ctClass.getTypeErasure())) {
            return;
        }
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
