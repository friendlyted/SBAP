package ru.sbsoft.sbap.plugin.xjc.sbap;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JFormatter;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.api.impl.NameConverter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.sbsoft.sbap.jaxb.adapters.QNameAdapter;
import ru.sbsoft.sbap.utils.jwa.JwaNamesUtils;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class SbapJaxbPlugin extends Plugin {

    public static final String DATA_NAMESPACE = "urn:sbap/definitions/data";
    public static final String META_NAMESPACE = "urn:sbap/definitions/meta";

    public static final List<String> NAMESPACES = Arrays.asList(DATA_NAMESPACE, META_NAMESPACE);

    public static final String APP_INFO = "appinfo";

    private static DocumentBuilder DOC_BUILDER;

    private static Map<String, JAXBContext> JAXB_CONTEXTS = new HashMap<>();

    public static JAXBContext getJAXB_CONTEXT(String namespace) {
        if (JAXB_CONTEXTS.containsKey(namespace)) {
            return JAXB_CONTEXTS.get(namespace);
        }

        JAXBContext newContext;
        try {
            newContext = JAXBContext.newInstance(convertToPackageName(namespace));
            JAXB_CONTEXTS.put(namespace, newContext);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        return newContext;
    }

    public static DocumentBuilder getDOC_BUILDER() {
        if (DOC_BUILDER == null) {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            try {
                DOC_BUILDER = factory.newDocumentBuilder();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        return DOC_BUILDER;
    }

    @Override
    public String getOptionName() {
        System.out.println("getOptionName");
        return "Xsbap";
    }

    @Override
    public String getUsage() {
        System.out.println("getUsage");
        return "-Xsbap : adding SBAP metadata into created classes";
    }

    @Override
    public List<String> getCustomizationURIs() {
        return NAMESPACES;
    }

    @Override
    public boolean isCustomizationTagName(String nsUri, String localName) {
//        try {
            return NAMESPACES.contains(nsUri);
            //unmarshall(stringToElement(String.format("<%s xmlns=\"%s\" />", localName, nsUri)));
            //return true;
        //} catch (SAXException | IOException | JAXBException ex) {
//            return false;
//        }
    }

    private Object unmarshall(Node node) throws JAXBException {
        final String namespaceURI = node.getNamespaceURI();
        if (NAMESPACES.contains(namespaceURI)) {
            final Unmarshaller unmarshaller = getJAXB_CONTEXT(namespaceURI).createUnmarshaller();
            unmarshaller.setAdapter(new QNameAdapter());
            return unmarshaller.unmarshal(node);
        }
        return null;
    }

    @Override
    public boolean run(Outline otln, Options optns, ErrorHandler eh) throws SAXException {
        try {
            otln.getClasses().stream().forEach(cl -> processClass(cl));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SAXException(ex);
        }

        return true;
    }

    private static Class<? extends Annotation> annotationByName(String annotationName) {
        try {
            return (Class<? extends Annotation>) Class.forName(annotationName);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private void processClass(final ClassOutline cl) {
        cl.target.getCustomizations().stream().forEach(cust -> {
            annotate(cl.ref, cust);
        });
        cl.ref.fields().values().stream()
                .forEach(f -> {
                    final CClassInfo target = cl.target;
                    final CPropertyInfo property = target.getProperty(f.name());

                    property.getCustomizations().stream().forEach(cust -> {
                        annotate(f, cust);
                    });

                    final List<CPluginCustomization> sublistCusts = new ArrayList<>();

                    if (!f.annotations().isEmpty()) {
                        for (JAnnotationUse a : f.annotations()) {
                            if (XmlElement.class.getName().equals(a.getAnnotationClass().fullName())) {
                                final JAnnotationValue name = a.getAnnotationMembers().get("name");

                                if (name != null) {
                                    final StringBuilder nameValue = new StringBuilder();
                                    name.generate(new JFormatter(new Writer() {
                                        @Override
                                        public void write(char[] cbuf, int off, int len) throws IOException {
                                            final String val = new String(cbuf, off, len).replaceAll("^\"|\"$", "");
                                            nameValue.append(val);
                                        }

                                        @Override
                                        public void flush() throws IOException {
                                        }

                                        @Override
                                        public void close() throws IOException {
                                        }
                                    }));

                                    for (CPluginCustomization cust : ((CElementPropertyInfo) property).ref().get(0).getCustomizations()) {
                                        sublistCusts.add(cust);
                                    }
                                }
                            }
                        }
                    }
                    sublistCusts.forEach(cust -> annotate(f, cust));
                });

    }

    private String annotationNameForCust(final CPluginCustomization cust) throws RuntimeException {
        final String packageName = convertToPackageName(cust.element.getNamespaceURI());
        final String typeName = capitalize(cust.element.getLocalName());
        return packageName + "." + typeName + "_A";
    }

    private static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    private void annotate(JAnnotatable annotatable, CPluginCustomization cust) {
        final Class<? extends Annotation> annotationClass = annotationByName(annotationNameForCust(cust));
        final JAnnotationUse annotation = annotatable.annotate(annotationClass);
        mergeElement(annotation, cust.element);
    }

    private void mergeElement(JAnnotationUse annotation, Element element) {
        final Object annotationModel;
        try {
            annotationModel = unmarshall(element);
            mergeObject(annotation, annotationModel);
        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private static String printNode(Node node) {
        final Document document = node.getOwnerDocument();
        final DOMImplementationLS domImplLS = (DOMImplementationLS) document
                .getImplementation();
        final LSSerializer serializer = domImplLS.createLSSerializer();
        return serializer.writeToString(node);
    }

    private void mergeObject(final JAnnotationUse annotation, final Object model) {
        if (model == null) {
            return;
        }
        Arrays.stream(model.getClass().getDeclaredFields())
                .filter(f -> !java.lang.reflect.Modifier.isStatic(f.getModifiers()))
                .forEach(f -> {
                    final String name = f.getName();
                    final Class type = f.getType();
                    final Object value;
                    try {
                        f.setAccessible(true);
                        value = f.get(model);

                        if (value == null) {
                            return;
                        }

                        if (type.isPrimitive()) {
                            annotation.param(name, value.toString());
                        } else if (type.isEnum()) {
                            annotation.param(name, (Enum) value);
                        } else if (String.class.equals(type)) {
                            annotation.param(name, (String) value);
                        } else if (int.class.equals(type)) {
                            annotation.param(name, (int) value);
                        } //TODO add other types
                        else if (Class.class.equals(type)) {
                            annotation.param(name, (Class) value);
                        } else if (Collection.class.isAssignableFrom(type)) {
                            final JAnnotationArrayMember array = annotation.paramArray(name);
                            for (Object item : (Collection) value) {
                                final JAnnotationUse au = array.annotate(JwaNamesUtils.annotizeClass(item.getClass()));
                                mergeObject(au, item);
                            }
                        } else {
                            final JAnnotationUse au = annotation.annotationParam(name, (Class<? extends Annotation>) Class.forName(type.getName().replaceAll("\\$", "\\.") + "_A"));
                            mergeObject(au, value);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                });
    }

    @Override
    public void postProcessModel(Model model, ErrorHandler errorHandler) {
        System.out.println("postProcessModel");

        model.beans().values().stream().forEach(bean -> {
            bean.getProperties().stream().forEach(prop -> {
                prop.getCustomizations().forEach(cust -> cust.markAsAcknowledged());
            });
            bean.getCustomizations().forEach(cust -> cust.markAsAcknowledged());
        });
    }

    public static String convertToPackageName(String namespace) {
        return new NameConverter.Standard().toPackageName(namespace);
    }

    public Element stringToElement(String string) throws SAXException, IOException {
        final StringReader reader = new StringReader(string);
        return getDOC_BUILDER().parse(new InputSource(reader)).getDocumentElement();
    }

}
