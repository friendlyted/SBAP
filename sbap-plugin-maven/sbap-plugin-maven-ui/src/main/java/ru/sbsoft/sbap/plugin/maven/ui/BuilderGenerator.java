package ru.sbsoft.sbap.plugin.maven.ui;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;
import ru.sbsoft.sbap.plugin.maven.ui.model.BuilderModel;
import ru.sbsoft.sbap.plugin.maven.ui.model.SetterModel;
import ru.sbsoft.sbap.system.utils.SbapReaderUtils;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class BuilderGenerator {

    public BuilderModel generateBuilderFor(final Object object, Function<Object, String> createBuilderMethodFor) {

        final Class iface = ifaceFor(object);
        if (iface == null || iface.equals(Object.class)) {
            throw new RuntimeException("No interface found for Class " + object.getClass());
        }

        final BuilderModel result = new BuilderModel();
        result.setName(builderNameFor(iface));

        Arrays.stream(iface.getMethods()).filter(m -> m.getName().startsWith("get") && m.getParameterCount() == 0).forEach(getter -> {
            try {
                final String setterName = "s" + getter.getName().substring(1);
                final Type returnType = getter.getGenericReturnType();
                final Class<?> returnClass = getter.getReturnType();
                final Method setter;
                try {
                    setter = iface.getMethod(setterName, returnClass);
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException("Interface must has setter for " + getter.getName());
                }

                if (setterName == null || setter.getParameterCount() != 1 || !setter.getGenericParameterTypes()[0].equals(returnType)) {
                    return;
                }

                final Object value = object.getClass().getMethod(getter.getName()).invoke(object);
                if (value == null) {
                    return;
                }

                if (returnClass.isPrimitive()) {
                    result.getSetters().add(new SetterModel(setterName, value.toString()));
                } else if (returnClass.isAssignableFrom(Boolean.class)) {
                    result.getSetters().add(new SetterModel(setterName, value.toString().toLowerCase()));
                } else if (returnClass.isAssignableFrom(String.class)) {
                    result.getSetters().add(new SetterModel(setterName, "\"" + value.toString().replaceAll("\"", "\\\\\"").replaceAll("\r?\n", "\\\\n\" +$0\"") + "\""));
                } else if (returnClass.isAssignableFrom(Date.class)) {
                    result.getSetters().add(new SetterModel(setterName, "new java.util.Date(" + ((Date) value).getTime() + "L)"));
                } else if (returnClass.isAssignableFrom(Class.class)) {
                    result.getSetters().add(new SetterModel(setterName, ((Class) value).getName() + ".class"));
                } else if (Collection.class.isAssignableFrom(returnClass) && (returnType instanceof ParameterizedType)) {
                    final ParameterizedType pType = (ParameterizedType) returnType;

                    final String adderName = "add" + getter.getName().substring(3).replaceAll("^set|List$|es$|s$", "");
                    for (Object item : (Collection) value) {
                        final Class<?> itemClass = item.getClass();
                        final String parameter;
                        if (itemClass.isPrimitive()) {
                            parameter = "" + item;
                        } else if (itemClass.isAssignableFrom(String.class)) {
                            parameter = "\"" + item.toString() + "\"";
                        } else if (itemClass.isAssignableFrom(Date.class)) {
                            parameter = "new java.util.Date(" + ((Date) item).getTime() + "L)";
                        } else if (itemClass.isAssignableFrom(Class.class)) {
                            parameter = ((Class) value).getName() + ".class";
                        } else {
                            parameter = createBuilderMethodFor.apply(item) + "(factoryContext)";
                        }
                        result.getSetters().add(new SetterModel(adderName, parameter));
                    }
                } else {
                    result.getSetters().add(new SetterModel(setterName, createBuilderMethodFor.apply(value) + "(factoryContext)"));
                }
            } catch (Exception ex) {
                throw new RuntimeException("Cannot extract value from class", ex);
            }
        });

        return result;
    }

    public static String builderNameFor(Class clazz) {
        return clazz.getName().replaceAll("\\$", "_") + "_Builder";
    }

    public static String builderNameFor(Object object) {
        return builderNameFor(object.getClass());
    }

    public static Class ifaceFor(Object object) {
        final Class clazz = object.getClass();
        final Class result = SbapReaderUtils.getInterfaceClassFor(object.getClass());
        if (result == null) {
            throw new RuntimeException("Class " + clazz + " is not SBAP-compatible");
        }
        return result;
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
