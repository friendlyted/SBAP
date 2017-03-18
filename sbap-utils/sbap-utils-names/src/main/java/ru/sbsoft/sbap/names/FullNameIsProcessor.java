package ru.sbsoft.sbap.names;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 *
 * @author Fedor Resnyanskiy
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("ru.sbsoft.sbap.names.FullNameIs")
public class FullNameIsProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<? extends Element> annotatedList = roundEnv.getElementsAnnotatedWith(FullNameIs.class);

        for (Element annotated : annotatedList) {
            final String name;
            if (annotated.getKind() == ElementKind.FIELD) {
                name = typeName(annotated.getEnclosingElement()) + "." + annotated.getSimpleName().toString();
            } else {
                name = typeName(annotated);
            }
            final FullNameIs fullNameIs = annotated.getAnnotation(FullNameIs.class);
            if (!fullNameIs.value().equals(name)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Полное название элемента " + name + " отличается от константы " + fullNameIs.value(),
                        annotated);
            }
        }
        return false;
    }

    private static String typeName(Element annotated) {
        return annotated.asType().toString().replaceAll("<.*", "");
    }

}
