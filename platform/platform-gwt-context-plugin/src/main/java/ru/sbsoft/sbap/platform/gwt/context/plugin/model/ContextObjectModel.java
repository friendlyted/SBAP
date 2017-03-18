package ru.sbsoft.sbap.platform.gwt.context.plugin.model;

import java.util.ArrayList;
import java.util.List;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@GenerateBuilder
public class ContextObjectModel {

    private String name;
    private String className;
    private List<MethodModel> methodList = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MethodModel> getMethodList() {
        return methodList;
    }

    public void setMethodList(List<MethodModel> methodList) {
        this.methodList = methodList;
    }

    @Override
    public String toString() {
        return "ContextObjectModel{" + "name=" + name + ", className=" + className + ", methodList=" + methodList + '}';
    }

}
