package ru.sbsoft.sbap.platform.gxt.utils;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sencha.gxt.widget.core.client.form.Field;
import com.sencha.gxt.widget.core.client.form.Validator;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.form.validator.EmptyValidator;
import ru.sbsoft.sbap.platform.gxt.components.FieldsContainer;

/**
 * Инструменты для работы с полями формы.
 *
 * @author balandin
 * @since Jul 3, 2013 4:12:39 PM
 */
public class FieldUtils {

    public static int FIELD_HEIGHT = 22;

    public static boolean hasEmptyValidator(Field field) {
        for (Object obj : field.getValidators()) {
            if (obj instanceof Validator) {
                if (((Validator) obj).getClass().equals(EmptyValidator.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static IsWidget createSeparator() {
        final SimplePanel separatorToolItem = new SimplePanel();
        separatorToolItem.setHeight("10px");
        separatorToolItem.setWidth("4px");
        return separatorToolItem;
    }

    public static IsWidget createSeparator(int width) {
        final SimplePanel separatorToolItem = new SimplePanel();
        separatorToolItem.setHeight("10px");
        separatorToolItem.setWidth(width + "px");
        return separatorToolItem;
    }

    public static FieldsContainer createFieldsContainer(ValueBaseField... fields) {
        if (fields == null || fields.length < 2 || fields.length > 10) {
            throw new IllegalArgumentException();
        }

        final FieldsContainer container = new FieldsContainer();

        final int count = fields.length;
        final double part = 1.00d / count;

        final HLD layotData = new HLD(part, -1);

        for (int i = 0; i < count; i++) {
            if (i > 0) {
                container.add(createSeparator());
            }
            container.add(fields[i], layotData);
        }

        return container;
    }
}
