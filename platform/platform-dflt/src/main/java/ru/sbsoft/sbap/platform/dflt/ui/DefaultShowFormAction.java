package ru.sbsoft.sbap.platform.dflt.ui;

import sbap.definitions.application.TShowFormAction_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaultShowFormAction implements TShowFormAction_I {

    private String formName;

    @Override
    public String getFormName() {
        return formName;
    }

    @Override
    public void setFormName(String value) {
        this.formName = value;
    }

}
