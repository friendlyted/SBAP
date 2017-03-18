package ru.sbsoft.sbap.plugin.apt.jwa.model;

import ru.sbsoft.sbap.plugin.apt.jwia.model.AnnotationUseModel;
import java.util.ArrayList;
import java.util.List;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@GenerateBuilder
public class AnnotationValueModel {

    private String name;
    private String stringValue;
    private List<AnnotationUseModel> arrayValue = new ArrayList<>();

    public AnnotationValueModel() {
    }

    public AnnotationValueModel(String name, String stringValue) {
        this.name = name;
        this.stringValue = stringValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public List<AnnotationUseModel> getArrayValue() {
        return arrayValue;
    }

    public void setArrayValue(List<AnnotationUseModel> arrayValue) {
        this.arrayValue = arrayValue;
    }

}
