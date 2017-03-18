package ru.sbsoft.sbap.names;

/**
 *
 * @author frekade
 */
public class NameConverter {

    public static String convertFieldNameToStaticName(String fieldName) {
        return "_" + fieldName.replaceAll("([A-Z])", "_$1").toUpperCase();
    }
}
