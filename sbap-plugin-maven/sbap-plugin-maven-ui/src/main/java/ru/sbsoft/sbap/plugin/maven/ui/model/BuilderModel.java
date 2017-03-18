package ru.sbsoft.sbap.plugin.maven.ui.model;

import java.util.ArrayList;
import java.util.List;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class BuilderModel {

    private String name;
    private List<SetterModel> setters;

    public BuilderModel() {
    }

    public BuilderModel(String name, List<SetterModel> setters) {
        this.name = name;
        this.setters = setters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SetterModel> getSetters() {
        if (setters == null) {
            setters = new ArrayList<>();
        }
        return setters;
    }

    public void setSetters(List<SetterModel> setters) {
        this.setters = setters;
    }

}
