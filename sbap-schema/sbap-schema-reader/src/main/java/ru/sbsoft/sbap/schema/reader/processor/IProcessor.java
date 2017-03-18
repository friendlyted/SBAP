package ru.sbsoft.sbap.schema.reader.processor;

import ru.sbsoft.sbap.schema.reader.model.ApplicationContext;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public interface IProcessor<T> {

    public void setContext(ApplicationContext context);

    public void process(T source);
}
