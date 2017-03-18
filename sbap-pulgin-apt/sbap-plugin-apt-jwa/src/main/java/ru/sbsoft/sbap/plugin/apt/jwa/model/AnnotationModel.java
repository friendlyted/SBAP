package ru.sbsoft.sbap.plugin.apt.jwa.model;

import ru.sbsoft.sbap.plugin.apt.jwia.model.AnnotationUseModel;
import java.util.ArrayList;
import java.util.List;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;
import ru.sbsoft.sbap.plugin.apt.jwia.model.MethodModel;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@GenerateBuilder
public class AnnotationModel {

    private String packageName;
    private String name;
    private List<MethodModel> methods = new ArrayList<>();
    private List<AnnotationModel> nestedAnnotations = new ArrayList<>();
    private List<AnnotationUseModel> annotations = new ArrayList<>();

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MethodModel> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodModel> methods) {
        this.methods = methods;
    }

    public List<AnnotationModel> getNestedAnnotations() {
        return nestedAnnotations;
    }

    public void setNestedAnnotations(List<AnnotationModel> nestedAnnotations) {
        this.nestedAnnotations = nestedAnnotations;
    }

    public List<AnnotationUseModel> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationUseModel> annotations) {
        this.annotations = annotations;
    }

}
