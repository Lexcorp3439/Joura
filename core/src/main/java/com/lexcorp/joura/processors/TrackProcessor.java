package com.lexcorp.joura.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.lexcorp.joura.Trackable;
import com.lexcorp.joura.templates.TrackableTemplate;

import spoon.processing.AbstractAnnotationProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;

public class TrackProcessor extends AbstractAnnotationProcessor<Trackable, CtClass<?>> {
    private final Logger log = Logger.getLogger(TrackProcessor1.class.getName());

    @Override
    public void process(Trackable trackable, CtClass<?> ctClass) {
        log.info("START");
        ctClass.addField(createTrackField());
        Set<CtMethod<?>> methods = ctClass.getAllMethods();
        List<CtField<?>> classFields = ctClass.getFields();
        for (CtMethod<?> method : methods) {
            List<CtField<?>> editableFields = getEditableFieldsFromMethod(method, classFields);
            if (editableFields.size() != 0) {
//                TrackableTemplate template = new TrackableTemplate(method.getBody());
//                CtBlock newBody = template.apply(method.getDeclaringType());
//                method.setBody(newBody);
                CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();


                final String value = String.format("if (this.NEED_TRACK) {System.out.println(\"Hello\");}",
                        method.getSimpleName(),
                        ctClass.getSimpleName());
                snippet.setValue(value);

                List<CtStatement> statements = method.getBody().getStatements();
                method.getBody().addStatement(method.getBody().getStatements().size() - 1, snippet);
            }
        }
    }

    private List<CtField<?>> getEditableFieldsFromMethod(CtMethod<?> method, List<CtField<?>> fields) {
        List<CtField<?>> editableFields = new ArrayList<>();
        method.getBody().getStatements().forEach((st) -> {
            st.getElements((element) -> {
                CtRole elementRole = element.getRoleInParent();
                if (elementRole.equals(CtRole.VARIABLE) || elementRole.equals(CtRole.DEFAULT_EXPRESSION)) {
                    if (element.getParent() instanceof CtFieldWrite) {
                        CtField<?> f = ((CtFieldWrite<?>) element.getParent()).getVariable().getFieldDeclaration();
                        if (fields.contains(f)) {
                            editableFields.add(f);
                        }
                    }
                }
                return element.getRoleInParent().equals(CtRole.VARIABLE);
            });
        });
        return editableFields;
    }

    private CtField<?> createTrackField() {
        Set<ModifierKind> modifierKinds = Collections.singleton(ModifierKind.PRIVATE);

        CtCodeSnippetExpression<Boolean> snippet1 =  getFactory().Core().createCodeSnippetExpression();
        snippet1.setType(getFactory().Type().BOOLEAN);
        snippet1.setValue("false");

        CtTypeReference<Boolean> reference = getFactory().Type().BOOLEAN;
        CtType<Boolean> type = reference.getTypeDeclaration();

        return (CtField<?>) getFactory().createField(type, modifierKinds, reference, "NEED_TRACK", snippet1);
    }


    @Override
    public void processingDone() {
        log.info("DONE");
    }
}
