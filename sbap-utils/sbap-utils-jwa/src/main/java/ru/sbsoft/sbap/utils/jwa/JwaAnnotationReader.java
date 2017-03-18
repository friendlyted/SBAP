package ru.sbsoft.sbap.utils.jwa;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class JwaAnnotationReader {

    public static <R> R read(JAnnotationUse annotation, Supplier<R> factory) {
        final R result = factory.get();

        try {
            annotation.getAnnotationMembers();
        } catch (NullPointerException ex) {
            return result;
        }

        annotation.getAnnotationMembers().forEach((name, aValue) -> {
            try {
                final Field field = result.getClass().getDeclaredField(name);
                boolean wasAccessible = field.isAccessible();
                field.setAccessible(true);

                field.set(result, readAnnotationField(field.getType(), getParamType(field), aValue));

                field.setAccessible(wasAccessible);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        });

        return result;
    }

    private static Object readAnnotationField(Class type, Class paramType, JAnnotationValue annotationValue) {

        if (JwaNamesUtils.classHasSbapInterface(type)) {
            final JAnnotationUse annotationUse = (JAnnotationUse) annotationValue;
            final JClass baseClass = JwaNamesUtils.baseClass(annotationUse.getAnnotationClass());

            return read(annotationUse, () -> createClassInstanceForName(baseClass.fullName()));
        } else if (List.class.isAssignableFrom(type)) {
            final List list = new ArrayList();
            for (JAnnotationValue item : ((JAnnotationArrayMember) annotationValue).annotations()) {
                list.add(readAnnotationField(paramType, null, item));
            }
            return list;
        } else if (String.class.equals(type)) {
            final String value = JClassUtils.readAnnotationField(annotationValue);
            return value.substring(1, value.length()-1);
        } else if (int.class.equals(type)) {
            return Integer.parseInt(JClassUtils.readAnnotationField(annotationValue));
        } //TODO add other types
        else if (Class.class.equals(type)) {
            try {
                final String className = JClassUtils.readAnnotationField(annotationValue);
                return Class.forName(className.replace(".class", ""));
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }

        return null;
    }

    private static Object createClassInstanceForName(String name) {
        try {
            final String packageName = name.replaceAll("([a-z0-9_\\.]+\\.).*", "$1");
            final String tail = name.replaceFirst(packageName, "");
            return Class.forName(packageName + tail.replaceAll("\\.", "\\$")).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Class getParamType(Field field) {
        if (!(field.getGenericType() instanceof ParameterizedType)) {
            return null;
        }
        final ParameterizedType paramType = (ParameterizedType) field.getGenericType();
        return (Class) paramType.getActualTypeArguments()[0];
    }

}
