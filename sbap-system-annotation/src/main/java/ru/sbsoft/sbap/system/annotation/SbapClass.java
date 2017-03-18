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
@FullNameIs(SbapClass.FULL_NAME)
@NameIs(SbapClass.NAME)
public @interface SbapClass {

    public static final String NAME = "SbapClass";
    public static final String FULL_NAME = "ru.sbsoft.sbap.system.annotation.SbapClass";

    Class value() default Object.class;
}
