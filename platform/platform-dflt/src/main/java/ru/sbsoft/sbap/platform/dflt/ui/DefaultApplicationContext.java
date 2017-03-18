package ru.sbsoft.sbap.platform.dflt.ui;

import ru.sbsoft.sbap.schema.ui.AbstractRegisters;
import ru.sbsoft.sbap.schema.ui.AbstractUIFactory;
import ru.sbsoft.sbap.schema.ui.ApplicationContext;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaultApplicationContext implements ApplicationContext {

    private AbstractUIFactory uiFactory;
    private AbstractRegisters registers;

    @Override
    public AbstractUIFactory getUiFactory() {
        return uiFactory;
    }

    @Override
    public void setUiFactory(AbstractUIFactory uiFactory) {
        this.uiFactory = uiFactory;
        uiFactory.setApplicationContext(this);
    }

    @Override
    public AbstractRegisters getRegisters() {
        return registers;
    }

    @Override
    public void setRegisters(AbstractRegisters registers) {
        this.registers = registers;
    }

}
