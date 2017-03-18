package ru.sbsoft.sbap.plugin.apt.jwia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class SbapAnnotationVisitor extends SimpleAnnotationValueVisitor8 {

    @Override
    protected Object defaultAction(Object o, Object p) {
        return o.toString();
    }

    @Override
    public Object visitArray(List vals, Object p) {
        final List result = new ArrayList();
        for (Object o : vals) {
            if (o instanceof AnnotationValue) {
                result.add(((AnnotationValue) o).accept(this, o));
            } else {
                throw new RuntimeException("AnnotationValue type expected.");
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> visitAnnotation(AnnotationMirror a, Object p) {
        final Map<String, Object> result = new HashMap<>();

        a.getElementValues().forEach((k, v) -> {
            final String name = k.getSimpleName().toString();
            final TypeMirror returnType = k.getReturnType();
            result.put(name, v.accept(this, v));
        });

        return result;
    }

    @Override
    public Object visitEnumConstant(VariableElement c, Object p) {
        return p.toString();
    }

}
