package ru.sbsoft.sbap.platform.gxt.ui.form;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.Viewport;
import sbap.definitions.application.Form_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class GxtForm implements Form_I, IsWidget {

    private Boolean readOnly;
    private Caption_I caption;
    private final Viewport viewport = new Viewport();
    private final HTML title = new HTML();

    public GxtForm() {
        viewport.add(title);
    }

    @Override
    public Caption_I getCaption() {
        return caption;
    }

    @Override
    public void setCaption(Caption_I value) {
        title.setHTML(value.getConstant().getValue());
        this.caption = value;
    }

    @Override
    public Widget asWidget() {
        return viewport;
    }

    @Override
    public void show() {
        final Window w = new Window();
        w.setSize("300px", "200px");
        w.add(this);

        w.show();
    }

    @Override
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public Boolean getReadOnly() {
        return readOnly;
    }

}
