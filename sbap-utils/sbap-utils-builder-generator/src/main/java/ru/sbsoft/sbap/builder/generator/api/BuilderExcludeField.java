package ru.sbsoft.sbap.builder.generator.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Fedor Resnyanskiy
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface BuilderExcludeField {

}
