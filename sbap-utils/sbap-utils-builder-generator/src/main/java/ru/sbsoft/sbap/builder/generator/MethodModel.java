package ru.sbsoft.sbap.builder.generator;

import javax.lang.model.element.ExecutableElement;

/**
 *
 * @author Fedor Resnyanskiy
 */
public class MethodModel {

    private ExecutableElement source;
    private String name;
    private String action;
    private String parametersWithTypes;
    private String parametersWithoutTypes;

    public ExecutableElement getSource() {
        return source;
    }

    public void setSource(ExecutableElement source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getParametersWithTypes() {
        return parametersWithTypes;
    }

    public void setParametersWithTypes(String parametersWithTypes) {
        this.parametersWithTypes = parametersWithTypes;
    }

    public String getParametersWithoutTypes() {
        return parametersWithoutTypes;
    }

    public void setParametersWithoutTypes(String parametersWithoutTypes) {
        this.parametersWithoutTypes = parametersWithoutTypes;
    }

}
