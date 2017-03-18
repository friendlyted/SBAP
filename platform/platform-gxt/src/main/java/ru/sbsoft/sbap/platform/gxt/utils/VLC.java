package ru.sbsoft.sbap.platform.gxt.utils;

import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

/**
 * Константы заполнения для
 * {@link com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer}.
 *
 * @author balandin
 * @since Jan 21, 2013 12:09:04 PM
 */
public class VLC {

    public static VerticalLayoutData FILL = new VerticalLayoutData(1, 1);
    public static VerticalLayoutData CONST = new VerticalLayoutData(1, -1);
    public static VerticalLayoutData CONST_M2 = new VerticalLayoutData(1, -1, new Margins(2));
    public static VerticalLayoutData CONST_M3 = new VerticalLayoutData(1, -1, new Margins(3));
}
