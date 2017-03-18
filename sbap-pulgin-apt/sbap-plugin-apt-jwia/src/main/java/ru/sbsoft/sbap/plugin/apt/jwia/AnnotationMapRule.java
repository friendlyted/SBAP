package ru.sbsoft.sbap.plugin.apt.jwia;

import java.util.function.Function;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class AnnotationMapRule {

    private String typeName;
    private Function<TypeMirror, String> typeProcessor;
    private Function<TypeMirror, String> defaultValueProcessor;

    public AnnotationMapRule() {
    }

    public AnnotationMapRule(String typeName, Function<TypeMirror, String> processor, Function<TypeMirror, String> defaultValueProcessor) {
        this.typeName = typeName;
        this.typeProcessor = processor;
        this.defaultValueProcessor = defaultValueProcessor;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Function<TypeMirror, String> getTypeProcessor() {
        return typeProcessor;
    }

    public void setTypeProcessor(Function<TypeMirror, String> typeProcessor) {
        this.typeProcessor = typeProcessor;
    }

    public Function<TypeMirror, String> getDefaultValueProcessor() {
        return defaultValueProcessor;
    }

    public void setDefaultValueProcessor(Function<TypeMirror, String> defaultValueProcessor) {
        this.defaultValueProcessor = defaultValueProcessor;
    }

}
