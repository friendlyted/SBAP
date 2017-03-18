package ru.sbsoft.sbap.platform.dflt.ui;

import sbap.definitions.application.TTreeItem_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaulTreeItemCaption implements TTreeItem_I.Caption_I {

    private String i18nCode;
    private String value;

    @Override
    public String getI18NCode() {
        return i18nCode;
    }

    @Override
    public void setI18NCode(String value) {
        this.i18nCode = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

}
