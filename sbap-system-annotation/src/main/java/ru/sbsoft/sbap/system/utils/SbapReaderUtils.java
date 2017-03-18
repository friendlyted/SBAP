package ru.sbsoft.sbap.system.utils;

import ru.sbsoft.sbap.system.annotation.SbapAnnotation;
import ru.sbsoft.sbap.system.annotation.SbapClass;
import ru.sbsoft.sbap.system.annotation.SbapInterface;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class SbapReaderUtils {

    private SbapReaderUtils() {
    }

    public static Class getInterfaceClassFor(Class clazz) {
        final SbapInterface sbapInterface = (SbapInterface) clazz.getAnnotation(SbapInterface.class);
        if (sbapInterface != null) {
            return sbapInterface.value();
        }

        try {
            return Class.forName(clazz.getName().replaceAll("\\$", "_I\\$") + "_I");
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static Class getAnnotationClassFor(Class clazz) {
        final SbapAnnotation sbapAnnotation = (SbapAnnotation) clazz.getAnnotation(SbapAnnotation.class);
        if (sbapAnnotation == null) {
            return null;
        }
        return sbapAnnotation.value();
    }

    public static Class getBaseClassFor(Class clazz) {
        final SbapClass sbapClass = (SbapClass) clazz.getAnnotation(SbapClass.class);
        if (sbapClass == null) {
            return null;
        }
        return sbapClass.value();
    }
}
