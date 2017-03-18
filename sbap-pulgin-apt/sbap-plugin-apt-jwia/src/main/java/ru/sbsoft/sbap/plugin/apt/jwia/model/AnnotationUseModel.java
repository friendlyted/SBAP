package ru.sbsoft.sbap.plugin.apt.jwia.model;

import java.util.ArrayList;
import java.util.List;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@GenerateBuilder
public class AnnotationUseModel {

    private String name;
    private List<AnnotationValueModel> values = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AnnotationValueModel> getValues() {
        return values;
    }

    public void setValues(List<AnnotationValueModel> values) {
        this.values = values;
    }

}
