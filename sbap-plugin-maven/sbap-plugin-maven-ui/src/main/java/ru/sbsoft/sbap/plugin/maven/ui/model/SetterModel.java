package ru.sbsoft.sbap.plugin.maven.ui.model;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class SetterModel {

    private String name;
    private String arg;

    public SetterModel() {
    }

    public SetterModel(String name, String arg) {
        this.name = name;
        this.arg = arg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

}
