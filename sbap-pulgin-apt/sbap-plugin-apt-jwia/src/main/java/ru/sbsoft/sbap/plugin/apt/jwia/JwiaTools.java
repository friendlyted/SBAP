package ru.sbsoft.sbap.plugin.apt.jwia;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import ru.sbsoft.sbap.plugin.apt.jwia.model.AnnotationUseModel;
import ru.sbsoft.sbap.plugin.apt.jwia.model.AnnotationUseModel_Builder;
import ru.sbsoft.sbap.plugin.apt.jwia.model.AnnotationValueModel;
import ru.sbsoft.sbap.plugin.apt.jwia.model.AnnotationValueModel_Builder;
import ru.sbsoft.sbap.system.BOOLEAN;
import ru.sbsoft.sbap.system.DEFAULT;
import ru.sbsoft.sbap.system.annotation.SbapAnnotation;
import ru.sbsoft.sbap.system.annotation.SbapClass;
import ru.sbsoft.sbap.system.annotation.SbapInterface;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class JwiaTools {

    private final static Map<String, AnnotationMapRule> ANNOTATIONS_MAP = new HashMap<>();

    public static void addAnnotationRule(String typeName, Function<TypeMirror, String> processor, Function<TypeMirror, String> defaultValueProcessor) {
        ANNOTATIONS_MAP.put(typeName, new AnnotationMapRule(typeName, processor, defaultValueProcessor));
    }

    public static AnnotationMapRule getAnnotationRuleFor(String typeName) {
        return ANNOTATIONS_MAP.get(typeName.replaceAll("<.*", ""));
    }

    static {
        addAnnotationRule(String.class.getName(), null, t -> "DEFAULT.DEFAULT_STRING");
        addAnnotationRule(Class.class.getName(), null, t -> "DEFAULT.class");
        addAnnotationRule(Object.class.getName(), t -> Class.class.getName(), t -> "DEFAULT.class");
        addAnnotationRule(List.class.getName(), t -> JwiaTools.annotizeList(t), t -> DEFAULT.DEFAULT_ARRAY_STRING);
        addAnnotationRule(Boolean.class.getName(), t -> BOOLEAN.FULL_NAME, t -> BOOLEAN.FULL_NAME + "." + BOOLEAN.NULL.name());
        addAnnotationRule(Integer.class.getSimpleName(), null, t -> "Integer.MIN_VALUE");
    }

    public static final String INTERFACE_SUFFIX = "_I";
    public static final String ANNOTATION_SUFFIX = "_A";

    public static String getInterfacePackageName(TypeElement typeElement, ProcessingEnvironment processingEnv) {
        return interfaze(typeElement.getEnclosingElement(), processingEnv);
    }

    public static String getAnnotationPackageName(TypeElement typeElement, ProcessingEnvironment processingEnv) {
        return annotize(typeElement.getEnclosingElement(), processingEnv);
    }

    public static AnnotationUseModel createClassLink(TypeElement typeElement) {
        return createAnnotationWithSingleValue(
                SbapClass.NAME,
                typeElement.asType().toString() + ".class"
        );
    }

    public static AnnotationUseModel createInterfaceLink(TypeElement typeElement) {
        return createAnnotationWithSingleValue(
                SbapInterface.NAME,
                addSuffix(typeElement.asType().toString(), INTERFACE_SUFFIX) + ".class"
        );
    }

    public static AnnotationUseModel createAnnotationLink(TypeElement typeElement) {
        return createAnnotationWithSingleValue(
                SbapAnnotation.NAME,
                addSuffix(typeElement.asType().toString(), ANNOTATION_SUFFIX) + ".class"
        );
    }

    public static AnnotationUseModel createSimpleAnnotation(String name) {
        return new AnnotationUseModel_Builder<>(new AnnotationUseModel())
                .setName(name)
                .get();
    }

    public static AnnotationUseModel createAnnotationWithSingleValue(String name, String value) {
        return new AnnotationUseModel_Builder<>(new AnnotationUseModel())
                .setName(name)
                .setValues(Collections.singletonList(
                        new AnnotationValueModel_Builder<>(new AnnotationValueModel())
                                .setName("value")
                                .setStringValue(value)
                                .get()
                ))
                .get();

    }

    public static String interfaze(String source) {
        return source + INTERFACE_SUFFIX;
    }

    public static String interfaze(Element element, ProcessingEnvironment processingEnv) {
        return interfaze(element.asType(), processingEnv);
    }

    public static String interfaze(TypeMirror type, ProcessingEnvironment processingEnv) {
        final String name = type.toString();
        if (Void.TYPE.getSimpleName().equals(name)) {
            return name;
        }
        if (type.getKind() == TypeKind.PACKAGE) {
            return name;
        }
        if (kindOf(type, processingEnv) == ElementKind.ENUM) {
            return name;
        }
        return addSuffix(name, INTERFACE_SUFFIX);
    }

    public static String annotize(String source) {
        return source + ANNOTATION_SUFFIX;
    }

    public static String annotize(Element element, ProcessingEnvironment processingEnv) {
        final TypeMirror type = element.asType();
        final String name = type.toString();
        if (type.getKind() == TypeKind.PACKAGE) {
            return name;
        }
        if (kindOf(type, processingEnv) == ElementKind.ENUM) {
            return name;
        }
        final AnnotationMapRule rule = getAnnotationRuleFor(name);
        if (rule == null || rule.getTypeProcessor() == null) {
            return addSuffix(name, ANNOTATION_SUFFIX);
        }
        return rule.getTypeProcessor().apply(type);

    }

    public static String annotizeList(TypeMirror type) {
        return listToArray(addSuffix(type.toString(), ANNOTATION_SUFFIX));
    }

    public static ElementKind kindOf(TypeMirror type, ProcessingEnvironment processingEnv) {
        return processingEnv.getTypeUtils().asElement(type).getKind();
    }

    public static String addSuffix(String className, String suffix) {
        final String prepared = className.replaceAll("(\\.|<|(?<!>)$|>|,)", suffix + "$1");
        final String clearPackage = prepared.replaceAll("((^|\\.|<\\s?|,\\s?)[a-z0-9_]+)" + suffix, "$1");
        final String clearJava = clearPackage.replaceAll("(java[\\.a-zA-Z0-9_]+)" + suffix, "$1");
        return clearJava;
    }

    public static String listToArray(String listName) {
        return listName.replaceAll("java.util.List<(.*)>", "$1[]");
    }

}
