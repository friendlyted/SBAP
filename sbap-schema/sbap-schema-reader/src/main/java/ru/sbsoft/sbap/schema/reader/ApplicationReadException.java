package ru.sbsoft.sbap.schema.reader;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class ApplicationReadException extends Exception {

    public ApplicationReadException() {
    }

    public ApplicationReadException(String message) {
        super(message);
    }

    public ApplicationReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationReadException(Throwable cause) {
        super(cause);
    }

}
