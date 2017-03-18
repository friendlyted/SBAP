package ru.sbsoft.sbap.platform.gxt.components;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.FormPanelHelper;
import ru.sbsoft.sbap.platform.gxt.utils.VLC;

/**
 * Набор полей, расположенных вертикально.
 *
 * @author Kiselev
 */
public class VerticalFieldSet extends FieldSet {

    final private VerticalLayoutContainer vContainer = new VerticalLayoutContainer();
    private int labelWidth = -1;

    public VerticalFieldSet() {
        super();
        setWidget(vContainer);
        getElement().setPadding(new Padding(3, 4, 0, 4));
    }

    public VerticalFieldSet(String caption) {
        this();
        setHeading(caption);
    }

    @Override
    public final void setHeading(String heading) {
        if (heading == null || "".equals(heading)) {
            super.setHeading("");
            return;
        }
        final StringBuffer b = new StringBuffer(heading == null ? "[ ]" : heading);
        b.setCharAt(0, b.substring(0, 1).toUpperCase().charAt(0));
        b.insert(0, ' ');
        b.append(' ');
        super.setHeading(b.toString());
    }

    public void addField(IsWidget child) {
        addField(child, VLC.CONST);
    }

    public void addField(IsWidget child, MarginData layoutData) {
        if (!(layoutData instanceof VerticalLayoutData)) {
            throw new UnsupportedOperationException();
        }
        if (null == child) {
            return;
        }
        if (child instanceof FieldLabel) {
            FieldLabel l = (FieldLabel) child;
            if (l.getWidget() instanceof CheckBox) {
                vContainer.add(child);
                return;
            }
        }

        vContainer.add(child, VLC.CONST);
    }

    @Override
    public void add(IsWidget child) {
        addField(child);
    }

    @Override
    public void add(Widget child) {
        addField(child);
    }

    @Override
    public void add(Widget child, MarginData layoutData) {
        addField(child, layoutData);
    }

    @Override
    public void clear() {
        vContainer.clear();
    }

    @Override
    public Widget getWidget(int index) {
        return vContainer.getWidget(index);
    }

    @Override
    public int getWidgetCount() {
        return vContainer.getWidgetCount();
    }

    @Override
    public int getWidgetIndex(IsWidget child) {
        return vContainer.getWidgetIndex(child);
    }

    @Override
    public int getWidgetIndex(Widget child) {
        return vContainer.getWidgetIndex(child);
    }

    @Override
    public boolean remove(int index) {
        return vContainer.remove(index);
    }

    @Override
    public boolean remove(IsWidget child) {
        return vContainer.remove(child);
    }

    @Override
    public boolean remove(Widget child) {
        return vContainer.remove(child);
    }

    public int getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
        updateLabelsWidth();
    }

    public void updateLabelsWidth() {
        if (labelWidth < 0) {
            return;
        }
        for (FieldLabel label : FormPanelHelper.getFieldLabels(this)) {
            label.setLabelWidth(labelWidth);
        }
    }

}
