package ru.sbsoft.sbap.plugin.maven.er;

import ru.sbsoft.sbap.plugin.maven.er.model.FieldInfo;
import ru.sbsoft.sbap.plugin.maven.er.model.ElementInfo;
import ru.sbsoft.sbap.plugin.maven.er.model.SchemaInfo;
import ru.sbsoft.sbap.plugin.maven.er.model.RelationInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sbap.definitions.er.ER;
import sbap.definitions.er.TRelationType;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@Mojo(name = "rich-xsd-relations", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ErMojo extends AbstractMojo {

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();
    private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private static Transformer TRANSFORMER;
    private static DocumentBuilder DOC_BUILDER;
    private static JAXBContext JAXB_CONTEXT;

    private final Pattern QNAME_REGEXP = Pattern.compile("(([a-zA-Z0-9_-]+):)?([a-zA-Z0-9_-]+)");

    /**
     * Директория, из которой читаются исходные XSD схемы.
     */
    @Parameter(defaultValue = "${basedir}/src/main/resources")
    private File basedir;

    /**
     * Маска файлов схем.
     */
    @Parameter(defaultValue = "**/*.xsd")
    private String[] modelSchemaList;

    
    @Parameter(defaultValue = "relations.xml")
    private String[] relationsList;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/er")
    private File output;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            project.addCompileSourceRoot(output.getAbsolutePath());

            final List<RelationInfo> relations = readRelationContent();
            final List<SchemaInfo> schemas = readSchemasContent();

            final Map<QName, ElementInfo> complexTypeDefinitionList = extractComplexTypesFrom(schemas);

            for (QName e : complexTypeDefinitionList.keySet()) {
                getLog().info("found complexType " + e.toString());
            }

            for (RelationInfo rel : relations) {
                getLog().info("found relation " + rel);

                final ElementInfo leftDefinition = complexTypeDefinitionList.get(rel.getLeft().getType());
                final ElementInfo rightDefinition = complexTypeDefinitionList.get(rel.getRight().getType());
                final QName leftQname = qnameForType(leftDefinition.getElement());
                final QName rightQname = qnameForType(rightDefinition.getElement());

                ensureImport(leftDefinition, rightDefinition);
                ensureImport(rightDefinition, leftDefinition);

                switch (rel.getType()) {
                    case ONE_TO_ONE:
                        addReference(leftDefinition, rel.getLeft().getField(), rightQname);
                        addReference(rightDefinition, rel.getRight().getField(), leftQname);
                        break;
                    case ONE_TO_MANY:
                        addReference(rightDefinition, rel.getRight().getField(), leftQname);
                        addList(leftDefinition, rel.getLeft().getField(), rightQname);
                        break;
                    case MANY_TO_MANY:
                        addList(leftDefinition, rel.getLeft().getField(), rightQname);
                        addList(rightDefinition, rel.getRight().getField(), leftQname);
                        break;
                    default:
                        break;
                }
            }

            schemas.forEach(schemaInfo -> {
                final File outputFile = new File(output, schemaInfo.getRelativePath());
                outputFile.getParentFile().mkdirs();
                try {
                    getTRANSFORMER().transform(
                            new DOMSource(schemaInfo.getDoc()),
                            new StreamResult(outputFile)
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

    }

    private void addList(ElementInfo elementInfo, String field, QName listQname) {
        if (field == null || field.isEmpty()) {
            return;
        }
        final Element list = elementInfo.getElement().getOwnerDocument().createElementNS(W3C_XML_SCHEMA_NS_URI, "element");
        list.setAttribute("name", field);
        addGeneratedMark(list);

        final Element listType = elementInfo.getElement().getOwnerDocument().createElementNS(W3C_XML_SCHEMA_NS_URI, "complexType");
        list.appendChild(listType);

        final Element sequence = elementInfo.getElement().getOwnerDocument().createElementNS(W3C_XML_SCHEMA_NS_URI, "sequence");
        listType.appendChild(sequence);

        final Element item = elementInfo.getElement().getOwnerDocument().createElementNS(W3C_XML_SCHEMA_NS_URI, "element");
        item.setAttribute("name", "item");
        item.setAttribute("xmlns:item", listQname.getNamespaceURI());
        item.setAttribute("type", "item:" + listQname.getLocalPart());
        item.setAttribute("maxOccurs", "unbounded");

        sequence.appendChild(item);

        final NodeList cn = elementInfo.getElement().getChildNodes();
        for (int i = 0; i < cn.getLength(); i++) {
            if ("sequence".equals(cn.item(i).getLocalName())) {
                cn.item(i).appendChild(list);
                return;
            }
        }

        final Element topSequence = elementInfo.getElement().getOwnerDocument().createElementNS(W3C_XML_SCHEMA_NS_URI, "sequence");
        elementInfo.getElement().insertBefore(topSequence, elementInfo.getElement().getFirstChild());
        topSequence.appendChild(list);
    }

    private void addReference(ElementInfo elementInfo, String field, QName refQname) {
        if (field == null || field.isEmpty()) {
            return;
        }
        final Document doc = elementInfo.getElement().getOwnerDocument();

        final Element item = doc.createElementNS(W3C_XML_SCHEMA_NS_URI, "element");
        item.setAttribute("name", field);
        item.setAttribute("xmlns:item", refQname.getNamespaceURI());
        item.setAttribute("type", "item:" + refQname.getLocalPart());

        addGeneratedMark(item);

        final NodeList cn = elementInfo.getElement().getChildNodes();
        for (int i = 0; i < cn.getLength(); i++) {
            if ("sequence".equals(cn.item(i).getLocalName())) {
                cn.item(i).appendChild(item);
                return;
            }
        }

        final Element topSequence = elementInfo.getElement().getOwnerDocument().createElementNS(W3C_XML_SCHEMA_NS_URI, "sequence");
        elementInfo.getElement().insertBefore(topSequence, elementInfo.getElement().getFirstChild());
        topSequence.appendChild(item);
    }

    private void addGeneratedMark(Element element) {
        final Document doc = element.getOwnerDocument();
        final Element annotation = doc.createElementNS(W3C_XML_SCHEMA_NS_URI, "annotation");
        element.appendChild(annotation);
        final Element appInfo = doc.createElementNS(W3C_XML_SCHEMA_NS_URI, "appinfo");
        annotation.appendChild(appInfo);
        final Element generated = doc.createElementNS("urn:sbap/definitions/meta", "generatedRelationField");
        appInfo.appendChild(generated);
    }

    private void ensureImport(ElementInfo target, ElementInfo reference) {
        final String targetNs = findTargetNamespace(target.getSchema());
        final String refNs = findTargetNamespace(reference.getSchema());
        if (targetNs.equals(refNs)) {
            return;
        }

        final NodeList imports = target.getSchema().getDoc().getElementsByTagNameNS(W3C_XML_SCHEMA_NS_URI, "import");
        for (int i = 0; i < imports.getLength(); i++) {
            final Element importElement = (Element) imports.item(i);
            if (refNs.equals(importElement.getAttribute("namespace"))) {
                return;
            }
        }

        final Element schema = getSchema(target.getSchema().getDoc());

        final Element importElement = target.getSchema().getDoc().createElementNS(W3C_XML_SCHEMA_NS_URI, "import");
        importElement.setAttribute("namespace", refNs);
//        importElement.setAttribute("schemaLocation", reference.getSchema().getRelativePath());

        schema.insertBefore(importElement, schema.getFirstChild());
    }

    private String findTargetNamespace(SchemaInfo schemaInfo) {
        return schemaInfo.getDoc().getDocumentElement().getAttribute("targetNamespace");
    }

    private Map<QName, ElementInfo> extractComplexTypesFrom(List<SchemaInfo> schemas) throws MojoExecutionException {
        final Map<QName, ElementInfo> result = new HashMap<QName, ElementInfo>();
        for (SchemaInfo schemaInfo : schemas) {
            try {
                final NodeList nodes = (NodeList) XPATH.evaluate("//*[local-name()='complexType']", schemaInfo.getDoc(), XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Element element = (Element) nodes.item(i);

                    if (element.hasAttribute("name")) {
                        final QName qname = qnameForType(element);
                        if (qname != null) {
                            final ElementInfo elementInfo = new ElementInfo();
                            elementInfo.setElement(element);
                            elementInfo.setSchema(schemaInfo);

                            result.put(qname, elementInfo);
                        }
                    }
                }
            } catch (XPathExpressionException ex) {
                getLog().error(ex);
                throw new MojoExecutionException("searching types in " + schemaInfo.getDoc() + " was failed", ex);
            }
        }
        return result;
    }

    private QName qnameForType(Element element) {
        final String localName = element.getAttribute("name");
        if (localName == null) {
            return null;
        }
        final String namespace = element.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
        return new QName(namespace, localName);
    }

    private List<RelationInfo> readRelationContent() throws MojoExecutionException {
        final List<RelationInfo> relations = new ArrayList<>();
        for (String relFileName : readRelations()) {
            final File relFile = new File(basedir, relFileName);

            try {
                final Document relDoc = getDOC_BUILDER().parse(relFile);
                final Element schema = getSchema(relDoc);
                final NodeList childNodes = schema.getChildNodes();

                for (int i = 0; i < childNodes.getLength(); i++) {
                    final Node node = childNodes.item(i);
                    if (!(node instanceof Element)) {
                        continue;
                    }
                    final Element relationElement = (Element) node;

                    final RelationInfo relationInfo = new RelationInfo();
                    relationInfo.setName(relationElement.getAttribute("name"));
                    relationInfo.setType(TRelationType.valueOf(relationElement.getAttribute("type")));

                    final FieldInfo leftField = new FieldInfo();
                    final QName leftType = readQname(relationElement, "leftType");
                    leftField.setType(leftType);
                    leftField.setPrefix(findNamespacePrefix(schema, ROLE));
                    leftField.setFile(findFileLocation(schema, leftType.getNamespaceURI()));
                    leftField.setField(relationElement.getAttribute("leftField"));
                    relationInfo.setLeft(leftField);

                    final FieldInfo rightField = new FieldInfo();
                    final QName rightType = readQname(relationElement, "rightType");
                    rightField.setType(rightType);
                    rightField.setPrefix(findNamespacePrefix(schema, ROLE));
                    rightField.setFile(findFileLocation(schema, rightType.getNamespaceURI()));
                    rightField.setField(relationElement.getAttribute("rightField"));
                    relationInfo.setRight(rightField);

                    relations.add(relationInfo);
                }
            } catch (Exception ex) {
                getLog().error(ex);
                throw new MojoExecutionException("Cannot unmarshal file " + relFileName, ex);
            }
        }
        return relations;
    }

    private String findFileLocation(Element schemaElement, String namespace) {
        final String locs = schemaElement.getAttributeNS(W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
        return locs.replaceAll(".*" + namespace + "\\s+([^\\s]+).*", "$1");
    }

    private String findNamespacePrefix(Element schemaElement, String namespace) {
        final NamedNodeMap attrs = schemaElement.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            final Node item = attrs.item(i);
            if (item.getTextContent().equals(namespace)) {
                return item.getNodeName().replaceAll("^xmlns:", "");
            }
        }
        return null;
    }

    private QName readQname(Element element, final String attrName) {
        final String attrValue = element.getAttribute(attrName);
        final Matcher matcher = QNAME_REGEXP.matcher(attrValue);
        matcher.find();
        final String nsPrefix = matcher.group(2);
        final String name = matcher.group(3);
        final String ns = getSchema(element).getAttribute("xmlns:" + nsPrefix);
        final QName qname = new QName(ns, name);
        return qname;
    }

    private Element getSchema(Node node) {
        if (node instanceof Document) {
            return (Element) ((Document) node).getChildNodes().item(0);
        }
        return (Element) node.getOwnerDocument().getChildNodes().item(0);
    }

    private String[] readRelations() {
        final DirectoryScanner relList = new DirectoryScanner();
        relList.setBasedir(basedir);
        relList.setIncludes(relationsList);
        relList.scan();

        return relList.getIncludedFiles();
    }

    private String[] readSchemas() {
        final DirectoryScanner msList = new DirectoryScanner();
        msList.setBasedir(basedir);
        msList.setIncludes(modelSchemaList);
        msList.scan();

        return msList.getIncludedFiles();
    }

    private List<SchemaInfo> readSchemasContent() throws MojoExecutionException {
        final List<SchemaInfo> result = new ArrayList<>();

        for (String msFileName : readSchemas()) {
            final File msFile = new File(basedir, msFileName);
            final Document msDoc;

            final SchemaInfo schemaInfo = new SchemaInfo();
            schemaInfo.setPath(msFile.getPath());

            try {
                msDoc = getDOC_BUILDER().parse(msFile);
                schemaInfo.setDoc(msDoc);
                schemaInfo.setRelativePath(msFileName);
                result.add(schemaInfo);
            } catch (Exception ex) {
                getLog().error(ex);
                throw new MojoExecutionException("cannot parse data file " + msFileName, ex);
            }
        }
        return result;
    }

    private static Schema schema(String name) throws SAXException {
        return SCHEMA_FACTORY.newSchema(ErMojo.class.getClassLoader().getResource(String.format("xsd/%s.xsd", name)));
    }

    private static JAXBContext getJAXB_CONTEXT() {
        if (JAXB_CONTEXT == null) {
            try {
                JAXB_CONTEXT = JAXBContext.newInstance(ER.class);

            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }

        return JAXB_CONTEXT;
    }

    private static DocumentBuilder getDOC_BUILDER() {
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

    private static Transformer getTRANSFORMER() {
        if (TRANSFORMER == null) {
            try {
                TRANSFORMER = TransformerFactory.newInstance().newTransformer();
                TRANSFORMER.setOutputProperty(OutputKeys.INDENT, "yes");
                TRANSFORMER.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            } catch (TransformerConfigurationException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        return TRANSFORMER;
    }

}
