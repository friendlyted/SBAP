package ru.sbsoft.sbap.platform.gxt.ui.application;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.Viewport;
import ru.sbsoft.sbap.platform.gxt.ui.tree.GxtTree;
import sbap.definitions.application.Application_I;
import sbap.definitions.application.TTree_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class GxtApplication implements Application_I, IsWidget {

    private Viewport viewport = new Viewport();
    private BorderLayoutContainer generalContainer = new BorderLayoutContainer();

    private ScrollPanel menuPanel = new ScrollPanel();
    private ScrollPanel workingPanel = new ScrollPanel();
    private HTML titlePanel = new HTML();

    private String applicationName;
    private String caption;

    private GxtTree mainMenu;

    public GxtApplication() {
        viewport.addStyleName("sbap-application");
        viewport.add(generalContainer);

        final BorderLayoutContainer.BorderLayoutData titleLayout = new BorderLayoutContainer.BorderLayoutData(50);
        //titleLayout.setCollapsible(true);
        titleLayout.setCollapseMini(true);

        final BorderLayoutContainer.BorderLayoutData menuLayout = new BorderLayoutContainer.BorderLayoutData();
        //menuLayout.setCollapsible(true);
        menuLayout.setCollapseMini(true);

        generalContainer.setNorthWidget(titlePanel, titleLayout);
        generalContainer.setWestWidget(menuPanel, menuLayout);
        generalContainer.setCenterWidget(workingPanel);

        titlePanel.addStyleName("sbap-application-caption");
        menuPanel.addStyleName("sbap-main-menu-panel");
        workingPanel.addStyleName("sbap-working-panel");

    }

    public VerticalPanel createMenuPanel() {
        return new VerticalPanel();
    }

    @Override
    public GxtTree getMainMenu() {
        return mainMenu;
    }

    @Override
    public void setMainMenu(TTree_I value) {
        mainMenu = (GxtTree) value;
        menuPanel.clear();
        menuPanel.add(mainMenu);
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        updateCaption(caption, applicationName);
    }

    @Override
    public String getCaption() {
        return titlePanel.getText();
    }

    @Override
    public void setCaption(String caption) {
        this.caption = caption;
        updateCaption(caption, applicationName);
    }

    @Override
    public Widget asWidget() {
        return viewport;
    }

    private void updateCaption(String caption, String name) {
        this.titlePanel.setText("" + (caption == null ? "" : caption) + " / " + (name == null ? "" : name));
    }

}
