package ru.sbsoft.sbap.schema.ui;

import sbap.definitions.meta.SbapContext_A;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@SbapContext_A
public interface AbstractRegister<T> {

    public T get(String name, ApplicationContext factoryContext);

    public T create(String name, ApplicationContext factoryContext);
}
