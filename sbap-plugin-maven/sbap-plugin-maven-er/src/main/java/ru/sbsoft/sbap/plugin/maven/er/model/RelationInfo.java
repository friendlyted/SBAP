package ru.sbsoft.sbap.plugin.maven.er.model;

import sbap.definitions.er.TRelationType;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public final class RelationInfo {

    private String name;
    private TRelationType type;
    private FieldInfo left;
    private FieldInfo right;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TRelationType getType() {
        return type;
    }

    public void setType(TRelationType type) {
        this.type = type;
    }

    public FieldInfo getLeft() {
        return left;
    }

    public void setLeft(FieldInfo left) {
        this.left = left;
    }

    public FieldInfo getRight() {
        return right;
    }

    public void setRight(FieldInfo right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return "RelationInfo{" + "name=" + name + ", type=" + type + ", left=" + left + ", right=" + right + '}';
    }

}
