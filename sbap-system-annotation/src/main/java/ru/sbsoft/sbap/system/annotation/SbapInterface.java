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
@NameIs(SbapInterface.NAME)
@FullNameIs(SbapInterface.FULL_NAME)
public @interface SbapInterface {

    public static final String NAME = "SbapInterface";
    public static final String FULL_NAME = "ru.sbsoft.sbap.system.annotation.SbapInterface";

    Class value() default Object.class;
}
