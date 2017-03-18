package ru.sbsoft.sbap.plugin.maven.binding;

import java.io.File;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@Mojo(name = "bind-xsd-to-jaxb", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class BindingMojo extends AbstractMojo {

    public static final String DATA = "urn:sbap/definitions/data";
    public static final String META = "urn:sbap/definitions/meta";

    @Parameter(defaultValue = "*.xsd")
    private List<String> sources;

    @Parameter(defaultValue = "${project.build.directory}/jaxb-binded")
    private File output;

    @Parameter(defaultValue = "src/main/resources")
    private File basedir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(basedir);
            scanner.setIncludes(sources.toArray(new String[0]));
            scanner.scan();

            for (String source : scanner.getIncludedFiles()) {
                processSource(source);
            }
        } catch (Exception ex) {
            getLog().error(ex);
            throw new MojoExecutionException("", ex);
        }
    }

    private void processSource(String source) throws Exception {
        final File srcFile = new File(basedir, source);
        final File dstFile = new File(output, source);

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.parse(srcFile);

        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPathExpression dataElementsXpath = xPathFactory.newXPath().compile("//*[namespace-uri()='" + DATA + "']");
        final XPathExpression metaElementsXpath = xPathFactory.newXPath().compile("//*[namespace-uri()='" + META + "']");

        boolean hasDataNamespace = false;
        String dataPrefix = "sbapData";
        final NamedNodeMap dataAttrs = doc.getDocumentElement().getAttributes();
        for (int i = 0; i < dataAttrs.getLength(); i++) {
            final Node attr = dataAttrs.item(i);
            if (DATA.equals(attr.getTextContent()) && attr.getPrefix().equals("xmlns")) {
                dataPrefix = attr.getLocalName();
                hasDataNamespace = true;
                break;
            }
        }

        boolean hasMetaNamespace = false;
        String metaPrefix = "sbapMeta";
        final NamedNodeMap metaAttrs = doc.getDocumentElement().getAttributes();
        for (int i = 0; i < metaAttrs.getLength(); i++) {
            final Node attr = metaAttrs.item(i);
            if (META.equals(attr.getTextContent()) && attr.getPrefix().equals("xmlns")) {
                metaPrefix = attr.getLocalName();
                hasMetaNamespace = true;
                break;
            }
        }

        boolean sbapUsing = false;
        final NodeList dataElements = (NodeList) dataElementsXpath.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < dataElements.getLength(); i++) {
            final Node item = dataElements.item(i);
            if (item instanceof Element) {
                replaceNodePrefix(item, DATA, dataPrefix);
                sbapUsing = true;
            }
        }

        final NodeList metaElements = (NodeList) metaElementsXpath.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < metaElements.getLength(); i++) {
            final Node item = metaElements.item(i);
            if (item instanceof Element) {
                replaceNodePrefix(item, META, metaPrefix);
                sbapUsing = true;
            }
        }

        if (sbapUsing) {
            doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:jaxb", "http://java.sun.com/xml/ns/jaxb");
            doc.getDocumentElement().setAttribute("jaxb:version", "1.0");
            doc.getDocumentElement().setAttribute("jaxb:extensionBindingPrefixes", dataPrefix + " " + metaPrefix);
            if (!hasDataNamespace) {
                doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + dataPrefix, DATA);
            }
            if (!hasMetaNamespace) {
                doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + metaPrefix, META);
            }
        }

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final Result output = new StreamResult(dstFile);
        final Source input = new DOMSource(doc);

        dstFile.getParentFile().mkdirs();
        transformer.transform(input, output);
    }

    private void replaceNodePrefix(Node node, String namespace, String prefix) {
        final NamedNodeMap attrs = node.getAttributes();

        for (int j = attrs.getLength() - 1; j >= 0; j--) {
            final Node attr = attrs.item(j);
            if (namespace.equals(attr.getTextContent())) {
                attrs.removeNamedItem(attr.getNodeName());
                break;
            }
        }
        node.setPrefix(prefix);
    }

}
