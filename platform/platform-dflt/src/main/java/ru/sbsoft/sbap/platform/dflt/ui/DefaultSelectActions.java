package ru.sbsoft.sbap.platform.dflt.ui;

import java.util.List;
import ru.sbsoft.sbap.platform.dflt.utils.ListeningList;
import sbap.definitions.application.TCustomScript_I;
import sbap.definitions.application.TShowFormAction_I;
import sbap.definitions.application.TStartOperationAction_I;
import sbap.definitions.application.TTreeItem_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaultSelectActions implements TTreeItem_I.SelectActions_I {

    private List<TShowFormAction_I> showFormActions = new ListeningList<>();
    private List<TStartOperationAction_I> startOperationActions = new ListeningList<>();
    private List<TCustomScript_I> customActions = new ListeningList<>();

    @Override
    public void setShowForm(List<TShowFormAction_I> showForm) {
        this.showFormActions.clear();
        this.showFormActions.addAll(showForm);
    }

    @Override
    public List<TShowFormAction_I> getShowForm() {
        return showFormActions;
    }

    @Override
    public void setStartOperation(List<TStartOperationAction_I> startOperation) {
        this.startOperationActions.clear();
        this.startOperationActions.addAll(startOperation);
    }

    @Override
    public List<TStartOperationAction_I> getStartOperation() {
        return startOperationActions;
    }

    @Override
    public void setCustom(List<TCustomScript_I> custom) {
        this.customActions.clear();
        this.customActions.addAll(custom);
    }

    @Override
    public List<TCustomScript_I> getCustom() {
        return customActions;
    }

}
