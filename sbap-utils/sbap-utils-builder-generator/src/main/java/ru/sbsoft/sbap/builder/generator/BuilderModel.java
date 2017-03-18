package ru.sbsoft.sbap.builder.generator;

import java.util.List;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author Fedor Resnyanskiy
 */
public class BuilderModel {

    private TypeElement source;
    private String name;
    private String targetClassName;
    private String packageName;
    private boolean iface;
    private List<MethodModel> methods;

    public TypeElement getSource() {
        return source;
    }

    public void setSource(TypeElement source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public void setTargetClassName(String targetClassName) {
        this.targetClassName = targetClassName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isIface() {
        return iface;
    }

    public void setIface(boolean iface) {
        this.iface = iface;
    }

    public List<MethodModel> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodModel> methods) {
        this.methods = methods;
    }

}
