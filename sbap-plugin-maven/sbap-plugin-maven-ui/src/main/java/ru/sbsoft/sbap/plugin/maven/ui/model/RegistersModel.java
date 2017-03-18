package ru.sbsoft.sbap.plugin.maven.ui.model;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class RegistersModel {

    private String packageName;
    private String treeRegister;
    private String formRegister;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTreeRegister() {
        return treeRegister;
    }

    public void setTreeRegister(String treeRegister) {
        this.treeRegister = treeRegister;
    }

    public String getFormRegister() {
        return formRegister;
    }

    public void setFormRegister(String formRegister) {
        this.formRegister = formRegister;
    }

}
