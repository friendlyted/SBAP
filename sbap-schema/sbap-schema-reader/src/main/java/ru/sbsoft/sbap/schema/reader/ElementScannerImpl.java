package ru.sbsoft.sbap.schema.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.codehaus.plexus.util.DirectoryScanner;
import org.xml.sax.SAXException;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class ElementScannerImpl implements ElementScanner {

    @Override
    public <T> List<T> scan(Class<T> clazz, String basedir, List<String> list) throws JAXBException, SAXException {
        return scan(clazz, basedir, list.toArray(new String[0]));
    }

    public <T> List<T> scan(Class<T> clazz, String basedir, String... list) throws JAXBException, SAXException {
        final DirectoryScanner scanner = scanner(basedir, list);
        final Unmarshaller unmarshaller = packageUnmarshaller(clazz);
        return parseAll(scanner, unmarshaller);
    }

    public static DirectoryScanner scanner(String basedir, String... list) {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(basedir);
        scanner.setIncludes(list);
        scanner.scan();
        return scanner;
    }

    public static <T> List<T> parseAll(DirectoryScanner scanner, Unmarshaller unmarshaller) throws JAXBException {
        final List<T> result = new ArrayList<>();
        for (String path : scanner.getIncludedFiles()) {
            result.add((T) unmarshaller.unmarshal(new File(scanner.getBasedir(), path)));
        }
        return result;
    }

    public static Unmarshaller packageUnmarshaller(Class clazz) throws SAXException, JAXBException {
        final JAXBContext jaxb = JAXBContext.newInstance(clazz.getPackage().getName());
        final Unmarshaller unmarshaller = jaxb.createUnmarshaller();
        return unmarshaller;
    }

}
