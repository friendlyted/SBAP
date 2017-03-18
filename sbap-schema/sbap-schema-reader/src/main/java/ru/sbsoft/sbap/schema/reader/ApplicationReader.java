package ru.sbsoft.sbap.schema.reader;

import java.util.List;
import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;
import ru.sbsoft.sbap.schema.reader.model.ApplicationContext;
import ru.sbsoft.sbap.schema.reader.model.SettingsModel;
import ru.sbsoft.sbap.schema.reader.processor.ApplicationProcessor;
import ru.sbsoft.sbap.schema.reader.processor.FormProcessor;
import ru.sbsoft.sbap.schema.reader.processor.TreeProcessor;
import sbap.definitions.application.Application;
import sbap.definitions.application.Form;
import sbap.definitions.application.Tree;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class ApplicationReader {

    private ElementScanner elementScanner;

    public ElementScanner getElementScanner() {
        if (elementScanner == null) {
            elementScanner = new ElementScannerImpl();
        }
        return elementScanner;
    }

    public void setElementScanner(ElementScannerImpl elementScanner) {
        this.elementScanner = elementScanner;
    }

    public ApplicationContext readApplication(SettingsModel settings) throws ApplicationReadException {
        final ApplicationContext context = new ApplicationContext();

        final ApplicationProcessor appProc = new ApplicationProcessor(context);
        final TreeProcessor treeProc = new TreeProcessor(context);
        final FormProcessor formProc = new FormProcessor(context);

        try {
            final Application app = getElementScanner().scan(Application.class, settings.getBasedir(), settings.getApplication()).get(0);
            appProc.process(app);

            final List<Tree> treeList = getElementScanner().scan(Tree.class, settings.getBasedir(), settings.getTrees());
            treeList.forEach(t -> t.setRegistered(true));
            treeList.forEach(t -> treeProc.process(t));

            final List<Form> formList = getElementScanner().scan(Form.class, settings.getBasedir(), settings.getForms());
            formList.forEach(f -> f.setRegistered(true));
            formList.forEach(f -> formProc.process(f));

            //
        } catch (JAXBException | SAXException ex) {
            throw new ApplicationReadException("Cannot read application files", ex);
        }

        return context;
    }
}
