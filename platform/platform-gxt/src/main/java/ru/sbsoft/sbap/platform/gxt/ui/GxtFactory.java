package ru.sbsoft.sbap.platform.gxt.ui;

import ru.sbsoft.sbap.platform.gxt.ui.form.GxtFieldset;
import ru.sbsoft.sbap.platform.gxt.ui.form.GxtTab;
import ru.sbsoft.sbap.platform.gxt.ui.form.GxtForm;
import ru.sbsoft.sbap.platform.gxt.ui.form.GxtField;
import ru.sbsoft.sbap.platform.dflt.ui.DefaultUIFactory;
import ru.sbsoft.sbap.platform.gxt.ui.action.GxtSelectAction;
import ru.sbsoft.sbap.platform.gxt.ui.application.GxtApplication;
import ru.sbsoft.sbap.platform.gxt.ui.tree.GxtDatasource;
import ru.sbsoft.sbap.platform.gxt.ui.tree.GxtTree;
import ru.sbsoft.sbap.platform.gxt.ui.tree.GxtTreeItem;
import sbap.definitions.application.Tree_I;

/**
 *
 * @author Fedor Resnyanskiy
 */
public class GxtFactory extends DefaultUIFactory {

    @Override
    public GxtApplication application() {
        return new GxtApplication();
    }

    @Override
    public GxtTree tree() {
        return new GxtTree(getApplicationContext());
    }

    @Override
    public GxtTreeItem tTreeItem() {
        return new GxtTreeItem();
    }

    @Override
    public GxtDatasource tDatasource() {
        return new GxtDatasource();
    }

    @Override
    public GxtSelectAction tSelectAction() {
        return new GxtSelectAction();
    }

    @Override
    public GxtForm form() {
        return new GxtForm();
    }

    @Override
    public GxtTab tTab() {
        return new GxtTab();
    }

    @Override
    public GxtFieldset tFieldset() {
        return new GxtFieldset();
    }

    @Override
    public GxtField tField() {
        return new GxtField();
    }

}
