package ru.sbsoft.sbap.platform.dflt.ui;

import ru.sbsoft.sbap.schema.ui.AbstractUIFactory;
import ru.sbsoft.sbap.schema.ui.ApplicationContext;
import sbap.definitions.application.TApplication_I;
import sbap.definitions.application.TCustomScript_I;
import sbap.definitions.application.TForm_I;
import sbap.definitions.application.TShowFormAction_I;
import sbap.definitions.application.TStartOperationAction_I;
import sbap.definitions.application.TTreeItem_I;
import sbap.definitions.application.TTree_I;
import sbap.definitions.data.TConstantValueType_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public abstract class DefaultUIFactory implements AbstractUIFactory {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public TApplication_I tApplication() {
        return application();
    }

    @Override
    public TTree_I tTree() {
        return tree();
    }

    @Override
    public TForm_I tForm() {
        return form();
    }

    @Override
    public TTreeItem_I.Caption_I tTreeItem_I_Caption() {
        return new DefaulTreeItemCaption();
    }

    @Override
    public TTreeItem_I.SelectActions_I tTreeItem_I_SelectActions() {
        return new DefaultSelectActions();
    }

    @Override
    public TShowFormAction_I tShowFormAction() {
        return new DefaultShowFormAction();
    }

    @Override
    public TStartOperationAction_I tStartOperationAction() {
        return new DefaultStartOperationAction();
    }

    @Override
    public TCustomScript_I tCustomScript() {
        return new DefaultCustomScript();
    }

    @Override
    public TForm_I.Caption_I tForm_I_Caption() {
        return new DefaultFormCaption();
    }

    @Override
    public TConstantValueType_I tConstantValueType() {
        return new DefaultConstantValueType();
    }

}
