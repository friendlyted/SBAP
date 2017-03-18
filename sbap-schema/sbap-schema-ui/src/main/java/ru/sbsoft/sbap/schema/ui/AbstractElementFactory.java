package ru.sbsoft.sbap.schema.ui;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public interface AbstractElementFactory<T> {

    T createElement(ApplicationContext factoryContext);
}
