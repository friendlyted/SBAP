package ru.sbsoft.sbap.system.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import ru.sbsoft.sbap.names.FullNameIs;
import ru.sbsoft.sbap.names.NameIs;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@Retention(RetentionPolicy.RUNTIME)
@NameIs(SbapAnnotation.NAME)
@FullNameIs(SbapAnnotation.FULL_NAME)
public @interface SbapAnnotation {

    public static final String NAME = "SbapAnnotation";
    public static final String FULL_NAME = "ru.sbsoft.sbap.system.annotation.SbapAnnotation";

    Class value() default Object.class;
}
