package ru.sbsoft.sbap.schema.ui;

import sbap.definitions.application.TSelectAction_I;
import sbap.definitions.application.Application_I;
import sbap.definitions.application.Form_I;
import sbap.definitions.application.TApplication_I;
import sbap.definitions.application.TCustomScript_I;
import sbap.definitions.application.TDatasource_I;
import sbap.definitions.application.TField_I;
import sbap.definitions.application.TFieldset_I;
import sbap.definitions.application.TForm_I;
import sbap.definitions.application.TShowFormAction_I;
import sbap.definitions.application.TStartOperationAction_I;
import sbap.definitions.application.TTab_I;
import sbap.definitions.application.TTree_I;
import sbap.definitions.application.TTreeItem_I;
import sbap.definitions.application.Tree_I;
import sbap.definitions.data.TConstantValueType_I;
import sbap.definitions.meta.SbapContext_A;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
// TODO: generate it
@SbapContext_A
public interface AbstractUIFactory {

    public void setApplicationContext(ApplicationContext context);

    public Application_I application();

    public TApplication_I tApplication();

    public Tree_I tree();

    public TTree_I tTree();

    public TTreeItem_I tTreeItem();

    public TDatasource_I tDatasource();

    public TSelectAction_I tSelectAction();

    public TShowFormAction_I tShowFormAction();

    public TStartOperationAction_I tStartOperationAction();

    public TCustomScript_I tCustomScript();

    public TTreeItem_I.Caption_I tTreeItem_I_Caption();

    public TTreeItem_I.SelectActions_I tTreeItem_I_SelectActions();

    public Form_I form();

    public TForm_I tForm();

    public TTab_I tTab();

    public TFieldset_I tFieldset();

    public TField_I tField();

    public TForm_I.Caption_I tForm_I_Caption();

    public TConstantValueType_I tConstantValueType();

}
