package ru.sbsoft.sbap.plugin.maven.er.model;

import javax.xml.namespace.QName;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public final class FieldInfo {

    private String prefix;
    private String file;
    private QName type;
    private String field;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "FieldInfo{" + "prefix=" + prefix + ", file=" + file + ", type=" + type + ", field=" + field + '}';
    }

}
