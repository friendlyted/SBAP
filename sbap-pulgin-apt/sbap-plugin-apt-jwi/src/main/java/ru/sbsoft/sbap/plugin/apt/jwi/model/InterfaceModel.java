package ru.sbsoft.sbap.plugin.apt.jwi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;
import ru.sbsoft.sbap.plugin.apt.jwia.model.AnnotationUseModel;
import ru.sbsoft.sbap.plugin.apt.jwia.model.MethodModel;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@GenerateBuilder
public class InterfaceModel {

    private String packageName;
    private String name;
    private String extendsName;
    private List<MethodModel> methods = new ArrayList<>();
    private List<InterfaceModel> nestedInterfaces = new ArrayList<>();
    private List<AnnotationUseModel> annotations = new ArrayList<>();
    private List<Map<String, Object>> actionList = new ArrayList<>();

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

    public String getExtendsName() {
        return extendsName;
    }

    public void setExtendsName(String extendsName) {
        this.extendsName = extendsName;
    }

    public List<MethodModel> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodModel> methods) {
        this.methods = methods;
    }

    public List<InterfaceModel> getNestedInterfaces() {
        return nestedInterfaces;
    }

    public void setNestedInterfaces(List<InterfaceModel> nestedInterfaces) {
        this.nestedInterfaces = nestedInterfaces;
    }

    public List<AnnotationUseModel> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationUseModel> annotations) {
        this.annotations = annotations;
    }

    public List<Map<String, Object>> getActionList() {
        return actionList;
    }

    public void setActionList(List<Map<String, Object>> actionList) {
        this.actionList = actionList;
    }

}
