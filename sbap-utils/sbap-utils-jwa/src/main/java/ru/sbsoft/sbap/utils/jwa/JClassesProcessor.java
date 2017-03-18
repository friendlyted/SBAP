package ru.sbsoft.sbap.utils.jwa;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.ClassOutline;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class JClassesProcessor {

    //Actions to execute after all
    private Function<JDefinedClass, JDefinedClass> classProcessor;
    private BiConsumer<JDefinedClass, JDefinedClass> classPostProcessor;

    private Consumer<GenData<JFieldVar>> fieldProcessor;
    private Predicate<GenData<JFieldVar>> ignoredField;

    private Consumer<GenData<JMethod>> methodProcessor;
    private Predicate<GenData<JMethod>> ignoredMethod;

    public void setClassProcessor(Function<JDefinedClass, JDefinedClass> classProcessor) {
        this.classProcessor = classProcessor;
    }

    public void setClassPostProcessor(BiConsumer<JDefinedClass, JDefinedClass> classPostProcessor) {
        this.classPostProcessor = classPostProcessor;
    }

    public void setFieldProcessor(Consumer<GenData<JFieldVar>> fieldProcessor) {
        this.fieldProcessor = fieldProcessor;
    }

    public void setIgnoredField(Predicate<GenData<JFieldVar>> ignoredField) {
        this.ignoredField = ignoredField;
    }

    public void setMethodProcessor(Consumer<GenData<JMethod>> methodProcessor) {
        this.methodProcessor = methodProcessor;
    }

    public void setIgnoredMethod(Predicate<GenData<JMethod>> ignoredMethod) {
        this.ignoredMethod = ignoredMethod;
    }

    public void process(Collection<? extends ClassOutline> classes) {
        final Map<JDefinedClass, JDefinedClass> newClasses = new HashMap<>();

        classes.stream().forEach(cl -> {
            final JDefinedClass target = classProcessor.apply(cl.ref);
            if (target != null) {
                newClasses.put(cl.ref, target);
            }
        });

        if (fieldProcessor != null) {
            for (Entry<JDefinedClass, JDefinedClass> e : newClasses.entrySet()) {
                e.getKey().fields().values().stream()
                        .map(f -> new GenData<>(e.getKey(), e.getValue(), f))
                        .filter(data -> ignoredField.test(data))
                        .forEach(data -> fieldProcessor.accept(data));
            }
        }

        if (methodProcessor != null) {
            for (Entry<JDefinedClass, JDefinedClass> e : newClasses.entrySet()) {
                e.getKey().methods().stream()
                        .map(m -> new GenData<>(e.getKey(), e.getValue(), m))
                        .filter(data -> ignoredMethod.test(data))
                        .forEach(data -> methodProcessor.accept(data));
            }
        }

        if (classPostProcessor != null) {
            newClasses.forEach((source, target) -> classPostProcessor.accept(source, target));
        }
    }

    public static final class GenData<T> {

        private JDefinedClass source;
        private JDefinedClass target;
        private T data;

        public GenData() {
        }

        public GenData(JDefinedClass source, JDefinedClass target, T data) {
            this.source = source;
            this.target = target;
            this.data = data;
        }

        public JDefinedClass getSource() {
            return source;
        }

        public void setSource(JDefinedClass source) {
            this.source = source;
        }

        public JDefinedClass getTarget() {
            return target;
        }

        public void setTarget(JDefinedClass target) {
            this.target = target;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

    }
}
