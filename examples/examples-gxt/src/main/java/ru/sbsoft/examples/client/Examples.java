package ru.sbsoft.examples.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import ru.sbsoft.examples.Registers;
import ru.sbsoft.examples.app1;
import ru.sbsoft.sbap.platform.dflt.ui.DefaultApplicationContext;
import ru.sbsoft.sbap.platform.gxt.ui.GxtFactory;
import ru.sbsoft.sbap.platform.gxt.ui.application.GxtApplication;
import ru.sbsoft.sbap.schema.ui.ApplicationContext;
import sbap.definitions.application.Application_I;

/**
 *
 * @author Fedor Resnyanskiy
 */
public class Examples implements EntryPoint {

    @Override
    public void onModuleLoad() {
        final ApplicationContext context = new DefaultApplicationContext();
        context.setUiFactory(new GxtFactory());
        context.setRegisters(new Registers());

        final Application_I app1 = new app1().createElement(context);
        final GxtApplication app = (GxtApplication) app1;

        RootPanel.get().add(app);
    }

}
