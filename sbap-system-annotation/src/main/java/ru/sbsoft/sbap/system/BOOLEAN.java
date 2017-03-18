package ru.sbsoft.sbap.system;

import ru.sbsoft.sbap.names.FullNameIs;

/**
 * Представление boolean с default-значением для использования внутри аннотаций.
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@FullNameIs(BOOLEAN.FULL_NAME)
public enum BOOLEAN {
    NULL,
    TRUE,
    FALSE;

    public static final String FULL_NAME = "ru.sbsoft.sbap.system.BOOLEAN";
}
