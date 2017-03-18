package ru.sbsoft.sbap.schema.ui;

import sbap.definitions.meta.SbapContext_A;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@SbapContext_A
public interface ApplicationContext {

    public AbstractUIFactory getUiFactory();

    public void setUiFactory(AbstractUIFactory uiFactory);

    public AbstractRegisters getRegisters();

    public void setRegisters(AbstractRegisters registers);

}
