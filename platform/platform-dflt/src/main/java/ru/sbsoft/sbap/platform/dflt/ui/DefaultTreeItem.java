package ru.sbsoft.sbap.platform.dflt.ui;

import java.util.List;
import ru.sbsoft.sbap.platform.dflt.utils.ListeningList;
import sbap.definitions.application.TTreeItem_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaultTreeItem implements TTreeItem_I {

    private Caption_I caption;
    private TTreeItem_I parent;
    private SelectActions_I selectActions;
    private final ListeningList<TTreeItem_I> childList = new ListeningList<>();

    @Override
    public Caption_I getCaption() {
        return caption;
    }

    @Override
    public void setCaption(Caption_I value) {
        this.caption = value;
    }

    @Override
    public TTreeItem_I getParent() {
        return parent;
    }

    @Override
    public void setParent(TTreeItem_I value) {
        this.parent = value;
    }

    @Override
    public List<TTreeItem_I> getChildList() {
        return childList;
    }

    @Override
    public void setChildList(List<TTreeItem_I> childList) {
        childList.clear();
        childList.addAll(childList);
    }

    @Override
    public SelectActions_I getSelectActions() {
        return this.selectActions;
    }

    @Override
    public void setSelectActions(SelectActions_I selectActions) {
        this.selectActions = selectActions;
    }

}
