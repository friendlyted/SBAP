package ru.sbsoft.sbap.schema.reader.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;
import sbap.definitions.application.TApplication;
import sbap.definitions.application.TForm;
import sbap.definitions.application.TTree;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@GenerateBuilder
public class ApplicationContext {

    private List<Consumer<TApplication>> applicationListener = new ArrayList<>();
    private List<Consumer<TTree>> treeListener = new ArrayList<>();

    private TApplication application;
    private List<TTree> treeList;
    private List<TForm> formList;

    public TApplication getApplication() {
        return application;
    }

    public void setApplication(TApplication application) {
        this.application = application;
    }

    public List<TTree> getTreeList() {
        if (treeList == null) {
            treeList = new ArrayList<>();
        }
        return treeList;
    }

    public void setTreeList(List<TTree> treeList) {
        this.treeList = treeList;
    }

    public List<TForm> getFormList() {
        if (formList == null) {
            formList = new ArrayList<>();
        }
        return formList;
    }

    public void setFormList(List<TForm> formList) {
        this.formList = formList;
    }

    public Runnable addApplicationListener(Consumer<TApplication> listener) {
        applicationListener.add(listener);
        return () -> applicationListener.remove(listener);
    }

    public Runnable addTreeListener(Consumer<TTree> listener) {
        treeListener.add(listener);
        return () -> treeListener.remove(listener);
    }

}
