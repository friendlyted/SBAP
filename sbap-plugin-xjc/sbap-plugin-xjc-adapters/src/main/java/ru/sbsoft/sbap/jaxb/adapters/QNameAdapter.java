package ru.sbsoft.sbap.jaxb.adapters;

import com.sun.xml.internal.bind.api.impl.NameConverter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class QNameAdapter extends XmlAdapter<QName, Class> {

    @Override
    public Class unmarshal(QName qName) throws Exception {
        if (qName.getNamespaceURI().equals(W3C_XML_SCHEMA_NS_URI)) {
            return convertXsdType(qName.getLocalPart());
        }
        final String pkg = convertToPackageName(qName.getNamespaceURI());
        final String name = convertToClassName(qName.getLocalPart());
        return Class.forName(pkg + "." + name);
    }

    @Override
    public QName marshal(Class v) throws Exception {
        return new QName("ns", "local");
    }

    public static String convertToPackageName(String namespace) {
        return new NameConverter.Standard().toPackageName(namespace);
    }

    public static String convertToClassName(String element) {
        return new NameConverter.Standard().toClassName(element);
    }

    private Class convertXsdType(String localPart) {
        switch (localPart) {
            case "string":
                return String.class;
            case "integer":
                return BigInteger.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "short":
                return short.class;
            case "decimal":
                return BigDecimal.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "QName":
                return Class.class;
            case "dateTime":
                return Date.class;
            case "date":
                return Date.class;
            case "time":
                return Date.class;
            case "g":
                return Date.class;
            case "base64Binary":
                return byte[].class;
            case "hexBinary":
                return byte[].class;
            case "unsignedInt":
                return long.class;
            case "unsignedShort":
                return int.class;
            case "unsignedByte":
                return short.class;
            case "anySimpleType":
                return Object.class;
            case "duration":
                return Duration.class;
            case "NOTATION":
                return QName.class;
            default:
                return Object.class;
        }
    }

}
