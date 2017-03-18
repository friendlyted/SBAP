package ru.sbsoft.examples.vaadin;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;
import javax.servlet.annotation.WebServlet;

/**
 *
 * @author Fedor Resnyanskiy
 */
@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = Application.class, productionMode = false, widgetset = "ru.sbsoft.sbf.App")
public class ApplicationServlet extends VaadinServlet {
    
}
