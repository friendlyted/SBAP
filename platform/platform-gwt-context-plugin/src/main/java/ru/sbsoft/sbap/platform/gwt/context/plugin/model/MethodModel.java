package ru.sbsoft.sbap.platform.gwt.context.plugin.model;

import java.util.ArrayList;
import java.util.List;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@GenerateBuilder
public class MethodModel {

    private String name;
    private List<ArgumentModel> argList = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ArgumentModel> getArgList() {
        return argList;
    }

    public void setArgList(List<ArgumentModel> argList) {
        this.argList = argList;
    }

    @Override
    public String toString() {
        return "MethodModel{" + "name=" + name + ", argList=" + argList + '}';
    }

}
