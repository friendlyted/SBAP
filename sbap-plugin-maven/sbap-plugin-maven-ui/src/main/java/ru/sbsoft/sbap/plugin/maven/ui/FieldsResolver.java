package ru.sbsoft.sbap.plugin.maven.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class FieldsResolver {

    private static Map<Object, String> objectNames = new HashMap<>();
    private static Map<Object, String> objectPackages = new HashMap<>();

    private static long counter = 1;

    public String resolveName(Object object) {
        if (objectNames.containsKey(object)) {
            return objectNames.get(object);
        }
        try {
            final Method getNameMethod = object.getClass().getMethod("getName");
            final String name = (String) getNameMethod.invoke(object);
            objectNames.put(object, name);
            return name;
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        }
        final String result = "Object_" + counter++;
        objectNames.put(object, result);
        return result;
    }

    public String resolvePackage(Object object) {
        if (objectPackages.containsKey(object)) {
            return objectPackages.get(object);
        }
        try {
            final Method getNameMethod = object.getClass().getMethod("getPackage");
            final String _package = (String) getNameMethod.invoke(object);
            objectPackages.put(object, _package);
            return _package;
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            objectPackages.put(object, null);
        }
        return null;
    }

    public boolean isRegistered(Object object) {
        try {
            final Method isRegisteredMethod = object.getClass().getMethod("getRegistered");
            if (isRegisteredMethod == null) {
                return false;
            }
            final Object result = isRegisteredMethod.invoke(object);
            if (result == null) {
                return false;
            }
            return (boolean) result;
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        }
        return false;
    }
}
