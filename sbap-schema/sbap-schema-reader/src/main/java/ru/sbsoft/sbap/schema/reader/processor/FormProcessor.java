package ru.sbsoft.sbap.schema.reader.processor;

import ru.sbsoft.sbap.schema.reader.model.ApplicationContext;
import sbap.definitions.application.TForm;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class FormProcessor extends DefaultProcessor<TForm> {

    public FormProcessor() {
    }

    public FormProcessor(ApplicationContext context) {
        super(context);

    }

    @Override
    public void process(TForm source) {
        getContext().getFormList().add(source);
    }
}
