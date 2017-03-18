package ru.sbsoft.sbap.utils.jwa;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.sbsoft.sbap.system.annotation.SbapInterface;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class JwaNamesUtils {

    public static final String ANNOTATION_SUFFIX = "_A";
    public static final String INTERFACE_SUFFIX = "_I";

    static final List<String> JAVA_KEYWORDS = Arrays.asList("abstract", "assert", "boolean",
            "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "extends", "false",
            "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while");

    private JwaNamesUtils() {
    }

    private static String convertNameWithSuffix(String name, String suffix) {
        return name + suffix;
    }

    public static String annotizeName(String name) {
        return convertNameWithSuffix(name, ANNOTATION_SUFFIX);
    }

    public static String interfazeName(String name) {
        return convertNameWithSuffix(name, INTERFACE_SUFFIX);
    }

    private static String convertNameWithSuffix(JClass clazz, String suffix) {
        final String pkg = clazz._package().name();
        StringBuilder sb = new StringBuilder("." + JwaNamesUtils.convertNameWithSuffix(clazz.name(), suffix));
        JClass outer = clazz.outer();
        while (outer != null) {
            sb.insert(0, JwaNamesUtils.convertNameWithSuffix(outer.name(), suffix)).insert(0, ".");
            outer = outer.outer();
        }
        return pkg + sb;
    }

    public static String annotizeName(JClass clazz) {
        return convertNameWithSuffix(clazz, ANNOTATION_SUFFIX);
    }

    public static String interfazeName(JClass clazz) {
        return convertNameWithSuffix(clazz, INTERFACE_SUFFIX);
    }

    private static JClass convertJClassWithSuffix(JClass clazz, String suffix) {
        final List<JClass> baseParents = new ArrayList<>();
        JClass outer = clazz.outer();
        while (outer != null) {
            baseParents.add(outer);
            outer = outer.outer();
        }

        Collections.reverse(baseParents);

        JDefinedClass tmp = null;

        for (JClass parent : baseParents) {
            tmp = tmp != null
                    ? JClassUtils.getNested(tmp, JwaNamesUtils.convertNameWithSuffix(parent.name(), suffix))
                    : clazz.owner()._getClass(JwaNamesUtils.convertNameWithSuffix(parent.fullName(), suffix));
        }

        if (tmp != null) {
            return JClassUtils.getNested(tmp, JwaNamesUtils.convertNameWithSuffix(clazz.name(), suffix));
        }

        final String fullInterfaceName = JwaNamesUtils.convertNameWithSuffix(clazz.fullName(), suffix);

        final JDefinedClass result = clazz.owner()._getClass(fullInterfaceName);
        if (result != null) {
            return result;
        }

        if (fullInterfaceName.startsWith("java")) {
            return null;
        }

        try {
            return clazz.owner().ref(Class.forName(fullInterfaceName));
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static JClass annotizeJClass(JClass clazz) {
        return convertJClassWithSuffix(clazz, ANNOTATION_SUFFIX);
    }

    public static JClass interfazeJClass(JClass clazz) {
        return convertJClassWithSuffix(clazz, INTERFACE_SUFFIX);
    }

    private static Class convertClassWithSuffix(Class clazz, String suffix) {
        try {
            return Class.forName(clazz.getName().replaceAll("\\$", suffix + "\\$") + suffix);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static Class<? extends Annotation> annotizeClass(Class clazz) {
        return convertClassWithSuffix(clazz, ANNOTATION_SUFFIX);
    }

    public static Class interfazeClass(Class clazz) {
        return convertClassWithSuffix(clazz, INTERFACE_SUFFIX);
    }

    public static Class interfaceBuilderForClass(Class clazz) {
        try {
            return Class.forName(clazz.getName().replaceAll("(\\$|$)", INTERFACE_SUFFIX + "_") + "_Builder");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static JClass baseClass(JClass clazz) {
        final String packageName = clazz.fullName().replaceAll("([a-z0-9_\\.]+)\\..*", "$1");
        final String tail = clazz.fullName().replaceFirst(packageName + "\\.", "").replaceAll("(" + ANNOTATION_SUFFIX + "|" + INTERFACE_SUFFIX + ")", "");
        JDefinedClass result = null;
        for (String name : tail.split("\\.")) {
            result = result != null
                    ? JClassUtils.getNested(result, name)
                    : clazz.owner()._getClass(packageName + "." + name);
        }
        if (result == null) {
            return clazz.owner().ref(packageName + "." + tail);
        }
        return result;
    }

    public static Class getSbapInterfaceClass(Class clazz) {
        final SbapInterface annotation = (SbapInterface) clazz.getAnnotation(SbapInterface.class);
        return annotation.value();
    }

    public static boolean classHasSbapInterface(Class clazz) {
        return clazz.isAnnotationPresent(SbapInterface.class);
    }

    public static JDefinedClass getSbapInterfaceClass(JDefinedClass clazz) {
        final JAnnotationUse annotation = JClassUtils.readAnnotation(clazz, SbapInterface.FULL_NAME);

        final JAnnotationValue value = annotation.getAnnotationMembers().get("value");
        final String className = JClassUtils.readAnnotationField(value).replaceAll("\\.class$", "");

        return findClass(clazz.owner(), className);
    }

    public static boolean classHasSbapInterface(JDefinedClass clazz) {
        return getSbapInterfaceClass(clazz) != null;
    }

    public static JClass interfazeGeneric(JType type) {
        if (type == null || !(type instanceof JClass) || !((JClass) type).isParameterized()) {
            return null;
        }

        final List<JClass> params = new ArrayList<>();

        for (JClass param : ((JClass) type).getTypeParameters()) {
            final JClass sameIface = interfazeJClass(param);
            if (sameIface != null) {
                params.add(sameIface);
            } else {
                params.add(param);
            }
        }

        return ((JClass) type).erasure().narrow(params);
    }

    public static boolean isIgnored(JFieldVar field) {
        final List<String> ignoreAnnotations = Arrays.asList(
                "sbap.definitions.meta.SbapIgnore_A",
                "sbap.definitions.meta.GeneratedRelationField_A"
        );
        return field.annotations().stream().anyMatch(a -> ignoreAnnotations.contains(a.getAnnotationClass().fullName()));
    }

    public static boolean isIgnored(JDefinedClass clazz, JMethod method) {
        String fieldName = method.name().replaceFirst("set|get|is", "");
        fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        if (JAVA_KEYWORDS.contains(fieldName)) {
            fieldName = "_" + fieldName;
        }
        final JFieldVar field = clazz.fields().get(fieldName);
        return field.annotations().stream().anyMatch(a -> a.getAnnotationClass().fullName().equals("sbap.definitions.meta.SbapIgnore_A"));
    }

    public static JDefinedClass findClass(JCodeModel model, String fullClassName) {
        final String packageName = fullClassName.replaceAll("^([a-z0-9_\\.]+)\\..*", "$1");
        final String classTail = fullClassName.replaceAll("^[a-z0-9_\\.]+\\.(.*)", "$1");
        final List<String> classChain = Arrays.asList(classTail.split("\\."));

        JDefinedClass result = null;
        for (String className : classChain) {
            if (result == null) {
                result = model._getClass(packageName + "." + className);
            } else {
                result = getNestedClass(result, className);
            }
        }
        return result;
    }

    public static JDefinedClass getNestedClass(JDefinedClass target, String className) {
        final Iterator<JDefinedClass> nestedClasses = target.classes();
        while (nestedClasses.hasNext()) {
            final JDefinedClass next = nestedClasses.next();
            if (next.name().equals(className)) {
                return next;
            }
        }
        return null;
    }

}
