package ru.sbsoft.sbap.platform.dflt.ui;

import sbap.definitions.application.TStartOperationAction_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaultStartOperationAction implements TStartOperationAction_I {

    private String operationName;

    @Override
    public String getOperationName() {
        return this.operationName;
    }

    @Override
    public void setOperationName(String value) {
        this.operationName = value;
    }

}
