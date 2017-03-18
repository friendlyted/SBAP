package ru.sbsoft.sbap.jaxb.adapters;

import java.util.Calendar;
import java.util.Date;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DateTimeAdapter extends XmlAdapter<String, Date> {

    @Override
    public Date unmarshal(final String value) throws Exception {
        if (value != null && !value.equals("")) {
            return DatatypeConverter.parseDateTime(value).getTime();
        } else {
            return null;
        }
    }

    @Override
    public String marshal(final Date value) {
        if (value != null) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(value);
            return DatatypeConverter.printDateTime(calendar);
        } else {
            return null;
        }
    }
}
