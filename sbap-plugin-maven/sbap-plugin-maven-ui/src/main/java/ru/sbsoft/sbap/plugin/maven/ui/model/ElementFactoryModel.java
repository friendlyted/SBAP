package ru.sbsoft.sbap.plugin.maven.ui.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class ElementFactoryModel {

    private String className;
    private String packageName;
    private String name;

    private BuilderMethodModel createMethod;
    private List<BuilderMethodModel> factoryMethods;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

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

    public BuilderMethodModel getCreateMethod() {
        return createMethod;
    }

    public void setCreateMethod(BuilderMethodModel createMethod) {
        this.createMethod = createMethod;
    }

    public List<BuilderMethodModel> getFactoryMethods() {
        if (factoryMethods == null) {
            factoryMethods = new ArrayList<>();
        }
        return factoryMethods;
    }

    public void setFactoryMethods(List<BuilderMethodModel> factoryMethods) {
        this.factoryMethods = factoryMethods;
    }

}
