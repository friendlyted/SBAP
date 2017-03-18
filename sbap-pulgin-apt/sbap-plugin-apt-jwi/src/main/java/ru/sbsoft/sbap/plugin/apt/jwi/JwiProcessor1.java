package ru.sbsoft.sbap.plugin.apt.jwi;

import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@SupportedAnnotationTypes("javax.annotation.Generated")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JwiProcessor1 extends JwiProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        super.process(annotations, roundEnv);
        return false;
    }

}
