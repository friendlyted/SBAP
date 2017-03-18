package ru.sbsoft.sbap.plugin.apt.jwia.model;

import java.util.ArrayList;
import java.util.List;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@GenerateBuilder
public class MethodModel {

    private String type;
    private String name;
    private String defaultValue;
    private List<ArgumentModel> arguments = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ArgumentModel> getArguments() {
        return arguments;
    }

    public void setArguments(List<ArgumentModel> arguments) {
        this.arguments = arguments;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
