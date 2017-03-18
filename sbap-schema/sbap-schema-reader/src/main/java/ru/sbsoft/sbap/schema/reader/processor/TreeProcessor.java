package ru.sbsoft.sbap.schema.reader.processor;

import ru.sbsoft.sbap.schema.reader.model.ApplicationContext;
import sbap.definitions.application.TTree;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class TreeProcessor extends DefaultProcessor<TTree> {

    public TreeProcessor() {
    }

    public TreeProcessor(ApplicationContext context) {
        super(context);
        context.addApplicationListener(
                application -> {
                    final TTree mainMenu = application.getMainMenu();
                    if (mainMenu.getRegistered()) {
                        process(mainMenu);
                    }
                }
        );
    }

    @Override
    public void process(TTree source) {
        getContext().getTreeList().add(source);
    }

}
