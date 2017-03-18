package ru.sbsoft.sbap.platform.dflt.ui;

import sbap.definitions.data.TConstantValueType_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaultConstantValueType implements TConstantValueType_I {

    private String value;

    @Override
    public String getI18NCode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setI18NCode(String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
