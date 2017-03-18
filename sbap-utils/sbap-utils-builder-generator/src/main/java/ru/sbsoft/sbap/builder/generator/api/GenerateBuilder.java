package ru.sbsoft.sbap.builder.generator.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ru.sbsoft.sbap.names.FullNameIs;

/**
 *
 * @author Fedor Resnyanskiy
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@FullNameIs(GenerateBuilder.GenerateBuilderName)
public @interface GenerateBuilder {

    public static final String GenerateBuilderName = "ru.sbsoft.sbap.builder.generator.api.GenerateBuilder";

    String name() default Defaults.DEFAULT_STRING;

    String packageName() default Defaults.DEFAULT_STRING;

    String[] includeMethods() default {};

    String[] excludeMethods() default {};
}
