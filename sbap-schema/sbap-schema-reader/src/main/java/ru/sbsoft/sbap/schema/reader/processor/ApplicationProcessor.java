package ru.sbsoft.sbap.schema.reader.processor;

import ru.sbsoft.sbap.schema.reader.model.ApplicationContext;
import sbap.definitions.application.Application;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class ApplicationProcessor extends DefaultProcessor<Application> {

    public ApplicationProcessor() {
    }

    public ApplicationProcessor(ApplicationContext context) {
        super(context);
    }

    @Override
    public void process(Application app) {
        getContext().setApplication(app);

    }

}
