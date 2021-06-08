package com.lexcorp.joura.compile.analysis.generators;

import java.util.List;
import java.util.stream.Collectors;

import com.lexcorp.joura.runtime.options.TrackField;
import com.lexcorp.joura.utils.CtHelper;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import static com.lexcorp.joura.utils.StringHelper.createFieldName;

public class FieldsGenerator {
    private final Factory factory;
    private final CtHelper ctHelper;
    private final CtClass<?> ctClass;
    private final List<CtField<?>> fields;

    public FieldsGenerator(CtClass<?> ctClass, List<CtField<?>> fields) {
        this.factory = ctClass.getFactory();
        this.ctHelper = new CtHelper(factory);
        this.ctClass = ctClass;
        this.fields = fields;
    }

    public CtField<Boolean> createClassTrackFieldIfNotAssigned(boolean defaultValue) {
        CtField<Boolean> trackField;
        List<CtField<?>> ctFields = this.fields.stream()
                .filter(f -> f.hasAnnotation(TrackField.class))
                .collect(Collectors.toList());
        if (ctFields.size() > 1) {
            throw new RuntimeException("Number of Track fields should not exceed 1");
        }
        if (ctFields.isEmpty()) {
            CtTypeReference<Boolean> typeReference = factory.Type().BOOLEAN;
            CtLiteral<Boolean> ctLiteral = ctHelper.createCtLiteral(defaultValue, typeReference);
            trackField = ctHelper.createCtField(createFieldName(), factory.Type().BOOLEAN, ctLiteral);
        } else {
            if (ctFields.get(0).getType().equals(factory.Type().BOOLEAN)
                    || ctFields.get(0).getType().equals(factory.Type().BOOLEAN_PRIMITIVE)) {
                trackField = (CtField<Boolean>) ctFields.get(0);
            } else {
                throw new RuntimeException("Type of Track fields should be boolean");
            }
        }
        trackField.setDocComment("Tracking flag");
        return trackField;
    }

    public CtField<String> createTagField() {
        CtField<String> identifierField;
        CtTypeReference<String> stringType = factory.Type().STRING;
        CtLiteral<String> ctLiteral = ctHelper.createCtLiteral("UNKNOWN", stringType);
        identifierField = ctHelper.createCtField(createFieldName(), stringType, ctLiteral);
        identifierField.setDocComment("Tag");
        return identifierField;
    }
}
