package ru.sbsoft.sbap.names;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 *
 * @author Fedor Resnyanskiy
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("ru.sbsoft.sbap.names.NameIs")
public class NameIsProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<? extends Element> annotatedList = roundEnv.getElementsAnnotatedWith(NameIs.class);

        for (Element annotated : annotatedList) {
            final String name = annotated.getSimpleName().toString().replaceAll("<.*", "");
            final NameIs nameIs = annotated.getAnnotation(NameIs.class);
            if (!nameIs.value().equals(name)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Название элемента " + name + " отличается от константы " + nameIs.value(),
                        annotated);
            }
        }
        return false;
    }

}
