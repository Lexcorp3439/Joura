package com.lexcorp.joura.compile.analysis.strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lexcorp.joura.compile.analysis.alias.AliasAnalyser;
import com.lexcorp.joura.compile.analysis.alias.Aliases;
import com.lexcorp.joura.logger.JouraLogger;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtFieldReference;

import static com.lexcorp.joura.logger.Markers.Compile.END_ALIAS_ANALYSIS_MARKER;
import static com.lexcorp.joura.logger.Markers.Compile.START_ALIAS_ANALYSIS_MARKER;

public class AliasStrategy extends AbstractStrategy {
    private AliasAnalyser aliasAnalyser;
    private static final JouraLogger logger = JouraLogger.get(AliasStrategy.class);

    @Override
    public void run() {
        logger.info(START_ALIAS_ANALYSIS_MARKER, this.ctClass.getSimpleName() + " " + this.classFields.toString());
        this.aliasAnalyser.run();
        logger.info(END_ALIAS_ANALYSIS_MARKER, this.ctClass.getSimpleName() + "\n");
    }

    @Override
    public AbstractStrategy setCtClass(CtClass<?> ctClass) {
        super.setCtClass(ctClass);
        this.aliasAnalyser = new AliasAnalyser(this.ctClass);
        return this;
    }

    @Override
    public List<CtField<?>> runForMethod(CtMethod<?> method) {
        if (this.classFields.isEmpty()) {
            return Collections.emptyList();
        }
        Set<CtField<?>> editableFields = new HashSet<>();

        if (method.getSimpleName().equals("referenceMethod")) {
            System.out.println();
        }
        method.getElements(e -> e instanceof CtAssignment)
                .forEach(e -> this.addUpdatedFieldsForCurrentIteration(
                        (CtAssignment<?, ?>) e, editableFields, aliasAnalyser.method(method)
                ));
        method.getElements(e -> e instanceof CtInvocation).stream()
                .map(e -> e.getElements(element -> element instanceof CtFieldReference))
                .forEach(e -> this.addFieldsPassedToMethod(e, editableFields, aliasAnalyser.method(method)));
        editableFields.retainAll(classFields);
        return new ArrayList<>(editableFields);
    }

    /**
     * Метод анализирует и добавляет те поля, которые ссылаются на this и были изменены присвоением
     *
     * @param ctAssignment   текущий ctAssignment кода
     * @param editableFields множество изменненных полей в методе
     */
    protected void addUpdatedFieldsForCurrentIteration(
            CtAssignment<?, ?> ctAssignment, Set<CtField<?>> editableFields, Aliases aliases) {
        CtExpression<?> assigned = ctAssignment.getAssigned();
        if (assigned instanceof CtFieldWrite) {
            CtFieldWrite<?> fieldWrite = (CtFieldWrite<?>) assigned;
            CtField<?> field = fieldWrite.getVariable().getFieldDeclaration();
            if (fieldWrite.getTarget() instanceof CtVariableRead) {
                if (fieldWrite.getTarget() instanceof CtFieldRead) {
                    CtFieldRead<?> ctFieldRead = (CtFieldRead<?>) fieldWrite.getTarget();
                    if (aliasAnalyser.isTrackable(ctFieldRead.getVariable().getType())) {
                        editableFields.add(field);
                    }
                } else {
//                    String varName = ((CtVariableRead<?>) fieldWrite.getTarget()).getVariable().getSimpleName();
                    String reference = fieldWrite.getTarget().toString();
                    boolean is_method_alias = aliases.isThisOrUnknownAlias(reference);
                    boolean is_class_field_alias = aliasAnalyser.fieldAliases.isThisOrUnknownAlias(reference);
                    if (is_method_alias || is_class_field_alias) {
                        editableFields.add(field);
                    }
                }
            } else {
                editableFields.add(field);
            }
        }
    }

    /**
     * Метод анализирует и добавляет те поля, которые ссылаются на this и переданы в какой-либо метод
     *
     * @param ctElementList  список
     * @param editableFields множество изменненных полей в методе
     */
    protected void addFieldsPassedToMethod(
            List<CtElement> ctElementList, Set<CtField<?>> editableFields, Aliases aliases
    ) {
        ctElementList.forEach(ctElement -> {
            CtFieldRead<?> fieldRead = (CtFieldRead<?>) ctElement.getParent();
            CtField<?> field = fieldRead.getVariable().getFieldDeclaration();
            if (fieldRead.getTarget() instanceof CtThisAccess) {
                editableFields.add(field);
            } else if (fieldRead.getTarget() instanceof CtVariableRead) {
                if (fieldRead.getTarget() instanceof CtFieldRead) {
                    CtFieldRead<?> ctFieldRead = (CtFieldRead<?>) fieldRead.getTarget();
                    if (aliasAnalyser.isTrackable(ctFieldRead.getVariable().getType())) {
                        editableFields.add(field);
                    }
                } else {
                    String reference = fieldRead.getTarget().toString();
                    boolean is_method_alias = aliases.isThisOrUnknownAlias(reference);
                    boolean is_class_field_alias = aliasAnalyser.fieldAliases.isThisOrUnknownAlias(reference);
                    if (is_method_alias || is_class_field_alias) {
                        editableFields.add(field);
                    }
                }
            }
        });
    }
}
