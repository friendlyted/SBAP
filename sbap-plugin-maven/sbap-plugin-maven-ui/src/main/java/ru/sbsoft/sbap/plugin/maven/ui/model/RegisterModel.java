package ru.sbsoft.sbap.plugin.maven.ui.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class RegisterModel {

    private String className;
    private String packageName;
    private String type;
    private Map<String, String> factories = new HashMap<>();

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addFactory(String name, String factoryName) {
        this.factories.put(name, factoryName);
    }

    public Map<String, String> getFactories() {
        return factories;
    }

}
