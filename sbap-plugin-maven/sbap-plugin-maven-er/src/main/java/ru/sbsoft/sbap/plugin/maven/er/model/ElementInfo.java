package ru.sbsoft.sbap.plugin.maven.er.model;

import org.w3c.dom.Element;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public final class ElementInfo {

    private SchemaInfo schema;
    private Element element;

    public SchemaInfo getSchema() {
        return schema;
    }

    public void setSchema(SchemaInfo schema) {
        this.schema = schema;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

}
