package ru.sbsoft.sbap.system;

import ru.sbsoft.sbap.names.FullNameIs;

/**
 * Различные знаения для использования по умолчанию в аннотациях.
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@FullNameIs(DEFAULT.FULL_NAME)
public final class DEFAULT {

    public static final String FULL_NAME = "ru.sbsoft.sbap.system.DEFAULT";

    /**
     * Строка по умолчанию
     */
    public static final String DEFAULT_STRING = "##_DEFAULT_STRING_##";
    /**
     * Класс по умолчанию
     */
    public static final Class DEFAULT_CLASS = DEFAULT.class;
    /**
     * Класс по умолчанию в строковом литерале
     */
    public static final String DEFAULT_CLASS_STRING = FULL_NAME + ".class";
    /**
     * Булин по умолчанию
     */
    public static final BOOLEAN DEFAULT_BOOLEAN = BOOLEAN.NULL;
    public static final String DEFAULT_BOOLEAN_STRING = BOOLEAN.FULL_NAME + ".NULL";
    public static final String DEFAULT_ARRAY_STRING = "{}";

}
