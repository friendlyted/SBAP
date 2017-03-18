package ru.sbsoft.sbap.jaxb.adapters;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DateAdapter extends XmlAdapter<String, Date> {

    public static final SimpleDateFormat DATE_FORMAT_yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Date unmarshal(final String value) throws Exception {
        if (value != null && !value.equals("")) {
            return DATE_FORMAT_yyyy_MM_dd.parse(value);
        } else {
            return null;
        }
    }

    @Override
    public String marshal(final Date value) {
        if (value != null) {
            return DATE_FORMAT_yyyy_MM_dd.format(value);
        } else {
            return null;
        }

    }

}
