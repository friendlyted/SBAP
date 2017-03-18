package ru.sbsoft.sbap.plugin.apt.jwia;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationValue;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class AnnotationModelBuilder<T> {

    private final T object;

    public AnnotationModelBuilder(T object) {
        this.object = object;
    }

    public AnnotationModelBuilder<T> set(String fieldName, AnnotationValue value) {
        try {
            final Object realValue = readValueFromAnnoation(value);
            final Class realClass = realValue.getClass();
            final Method method = object.getClass().getMethod("set" + capitalize(fieldName), realClass);
            method.invoke(object, realValue);

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return this;
    }

    public T get() {
        return object;
    }

    private Object readValueFromAnnoation(AnnotationValue value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String capitalize(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
