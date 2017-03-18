package ru.sbsoft.sbap.utils.jwa;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class JClassUtils {

    private JClassUtils() {
    }

    public static JDefinedClass getNested(JDefinedClass outer, String innerName) {
        final Optional<JDefinedClass> result = iteratorToStream(outer.classes()).filter(c -> c.name().equals(innerName)).findFirst();
        if (!result.isPresent()) {
            return null;
        }
        return result.get();
    }

    public static boolean containsNested(JDefinedClass outer, JClass inner) {
        return iteratorToStream(outer.classes())
                .anyMatch(c -> c.equals(inner));
    }

    public static <T> Stream<T> iteratorToStream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    public static JAnnotationUse readAnnotation(JDefinedClass clazz, String name) {
        final Optional<JAnnotationUse> annotation = clazz.annotations().stream()
                .filter(a -> {
                    return a.getAnnotationClass().fullName().equals(name);
                })
                .findAny();
        if (!annotation.isPresent()) {
            return null;
        }
        return annotation.get();
    }

    public static String readAnnotationField(JAnnotationValue annotationValue) {
        final StringWriter writer = new StringWriter();
        annotationValue.generate(new JFormatter(writer));
        return writer.toString();
    }
}
