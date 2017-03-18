package ru.sbsoft.sbap.plugin.maven.er.model;

import org.w3c.dom.Document;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public final class SchemaInfo {

    private String relativePath;
    private String path;
    private Document doc;

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

}
