package ru.sbsoft.sbap.schema.reader.model;

import java.util.List;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@GenerateBuilder
public class SettingsModel {

    private String basedir;
    private List<String> trees;
    private List<String> forms;
    private String application;

    public String getBasedir() {
        return basedir;
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public List<String> getTrees() {
        return trees;
    }

    public void setTrees(List<String> trees) {
        this.trees = trees;
    }

    public List<String> getForms() {
        return forms;
    }

    public void setForms(List<String> forms) {
        this.forms = forms;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

}
