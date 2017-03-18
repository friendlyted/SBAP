package ru.sbsoft.sbap.platform.dflt.ui;

import sbap.definitions.application.TForm_I;
import sbap.definitions.data.TConstantValueType_I;
import sbap.definitions.data.TModelFieldValueType_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaultFormCaption implements TForm_I.Caption_I {

    private TConstantValueType_I constant;

    @Override
    public TConstantValueType_I getConstant() {
        return constant;
    }

    @Override
    public void setConstant(TConstantValueType_I value) {
        this.constant = value;
    }

    @Override
    public TModelFieldValueType_I getModelField() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setModelField(TModelFieldValueType_I value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
