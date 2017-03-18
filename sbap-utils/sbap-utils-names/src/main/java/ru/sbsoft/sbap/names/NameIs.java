package ru.sbsoft.sbap.names;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Fedor Resnyanskiy
 */
@Retention(RetentionPolicy.SOURCE)
public @interface NameIs {

    String value();
}
