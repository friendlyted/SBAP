package ru.sbsoft.sbap.platform.gxt.components;

import com.google.gwt.dom.client.Style;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import ru.sbsoft.sbap.platform.gxt.utils.FieldUtils;

/**
 * Представляет собой {@link HorizontalLayoutContainer} со слегка адапрированным поведением.
 * @author balandin
 * @since Oct 11, 2013 4:59:32 PM
 */
public class FieldsContainer extends HorizontalLayoutContainer {

    public FieldsContainer() {
    }

    @Override
    protected void onAfterFirstAttach() {
        super.onAfterFirstAttach();
        getParent().setHeight(""+FieldUtils.FIELD_HEIGHT+Style.Unit.PX.getType());
    }

    @Override
    public void setPixelSize(int width, int height) {
        super.setPixelSize(width, height);
        forceLayout();
    }
}
