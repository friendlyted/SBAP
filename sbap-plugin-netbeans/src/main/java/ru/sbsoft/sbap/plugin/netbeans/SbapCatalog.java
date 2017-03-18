package ru.sbsoft.sbap.plugin.netbeans;

//import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import static ru.sbsoft.sbap.plugin.netbeans.SbapCatalog.SBAP_SCHEMA.*;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class SbapCatalog implements CatalogReader, CatalogDescriptorBase { //, CatalogDescriptor  {

    public static final String NB_STORAGE = "nbres:/ru/sbsoft/sbap/plugin/netbeans/resources/";

    public static final String NAMESPACE_PREFIX = "urn:sbap/definitions/";
    public static final String SCHEMA_EXT = ".xsd";

    public static final String DATA_DIR = "xsd/";
    public static final String META_DIR = "xsd/";
    public static final String ER_DIR = "xsd/";
    public static final String UI_DIR = "er/";

    public static enum SBAP_SCHEMA {
        SBAP_DATA_SCHEMA(DATA_DIR, "data"),
        SBAP_META_SCHEMA(META_DIR, "meta"),
        SBAP_RELATION_SCHEMA(ER_DIR, "er"),
        SBAP_APPLICATION_SCHEMA(UI_DIR, "application");

        private final String dir;
        private final String name;

        private SBAP_SCHEMA(String dir, String name) {
            this.dir = dir;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getNamespace() {
            return NAMESPACE_PREFIX + name;
        }

        public String getFile() {
            return NB_STORAGE + dir + name + SCHEMA_EXT;
        }

        public static SBAP_SCHEMA fromNamespace(String namespace) {
            for (SBAP_SCHEMA s : values()) {
                if (namespace.endsWith(s.getName())) {
                    return s;
                }
            }
            return null;
        }
    }

    @Override
    public Iterator getPublicIDs() {
        return Arrays.stream(SBAP_SCHEMA.values())
                .map(v -> v.getNamespace())
                .collect(Collectors.toList())
                .iterator();
    }

    @Override
    public void refresh() {

    }

    @Override
    public String getSystemID(String publicId) {
        return SBAP_SCHEMA.fromNamespace(publicId).getFile();
    }

    @Override
    public String resolveURI(String string) {
        final String cleaned = string.replaceAll("\\?.*", "");
        if (!cleaned.startsWith("urn")) {
            return cleaned;
        }
        return getSystemID(cleaned);
    }

    @Override
    public String resolvePublic(String string) {
        return null;
    }

    @Override
    public void addCatalogListener(CatalogListener cl) {
    }

    @Override
    public void removeCatalogListener(CatalogListener cl) {
    }

    @Override
    public String getDisplayName() {
        return "SBAP schemas";
    }

    @Override
    public String getShortDescription() {
        return "SBAP schemas";
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
    }

}
