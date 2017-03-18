package ru.sbsoft.sbap.platform.gxt.components;

import com.google.gwt.dom.client.Style;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import ru.sbsoft.sbap.platform.gxt.utils.FieldUtils;
import sbap.definitions.meta.SbapContext_A;

/**
 * Компонент CheckBox с настроенным внешним видом.
 *
 * @author balandin
 */
@SbapContext_A
public class CheckBoxField extends CheckBox {

    public CheckBoxField() {
        this(null);
    }

    public CheckBoxField(String label) {
        super();

        if (label != null) {
            setBoxLabel(label);
        }

        getElement().getStyle().setHeight(FieldUtils.FIELD_HEIGHT, Style.Unit.PX);
        getElement().getStyle().setWidth(16, Style.Unit.PX);
        getElement().getStyle().setPaddingTop(5, Style.Unit.PX);
        getElement().getStyle().setMarginBottom(-5, Style.Unit.PX);
    }
}
