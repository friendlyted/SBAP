package ru.sbsoft.sbap.schema.reader.processor;

import ru.sbsoft.sbap.schema.reader.model.ApplicationContext;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaultProcessor<T> implements IProcessor<T> {

    private ApplicationContext context;

    public DefaultProcessor() {
    }

    public DefaultProcessor(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public void process(T source) {
    }

}
