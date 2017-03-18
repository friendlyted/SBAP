package ru.sbsoft.sbap.schema.ui;

import sbap.definitions.meta.SbapContext_A;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@SbapContext_A
public interface AbstractRegisters {

    AbstractRegister<sbap.definitions.application.TTree_I> trees();

    AbstractRegister<sbap.definitions.application.TForm_I> forms();
}
