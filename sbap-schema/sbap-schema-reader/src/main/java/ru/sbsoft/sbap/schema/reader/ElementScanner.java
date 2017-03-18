package ru.sbsoft.sbap.schema.reader;

import java.util.List;
import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public interface ElementScanner {

    <T> List<T> scan(Class<T> clazz, String basedir, List<String> list) throws JAXBException, SAXException;

    <T> List<T> scan(Class<T> clazz, String basedir, String... list) throws JAXBException, SAXException;
}
