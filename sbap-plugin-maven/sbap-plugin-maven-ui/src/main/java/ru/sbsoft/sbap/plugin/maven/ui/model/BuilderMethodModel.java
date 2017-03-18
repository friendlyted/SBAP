package ru.sbsoft.sbap.plugin.maven.ui.model;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class BuilderMethodModel {

    private String name;
    private String returnType;
    private String factoryMethod;
    private BuilderModel builder;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getFactoryMethod() {
        return factoryMethod;
    }

    public void setFactoryMethod(String factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    public BuilderModel getBuilder() {
        return builder;
    }

    public void setBuilder(BuilderModel builder) {
        this.builder = builder;
    }

}
