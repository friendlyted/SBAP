package ru.sbsoft.sbap.plugin.apt.jwa;

import ru.sbsoft.sbap.plugin.apt.jwia.AnnotationMapRule;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import ru.sbsoft.sbap.plugin.apt.jwia.JwiaTools;
import ru.sbsoft.sbap.plugin.apt.jwia.VelocityTools;
import ru.sbsoft.sbap.plugin.apt.jwa.model.AnnotationModel;
import ru.sbsoft.sbap.plugin.apt.jwa.model.AnnotationModel_Builder;
import ru.sbsoft.sbap.plugin.apt.jwia.model.MethodModel;
import ru.sbsoft.sbap.plugin.apt.jwia.model.MethodModel_Builder;
import ru.sbsoft.sbap.system.annotation.SbapAnnotation;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@SupportedAnnotationTypes("javax.annotation.Generated")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JwaProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            final Set<? extends Element> generatedList = roundEnv.getElementsAnnotatedWith(Generated.class);

            generatedList.stream()
                    .filter(e -> e.getKind() == ElementKind.CLASS && e.getEnclosingElement().getKind() == ElementKind.PACKAGE)
                    .map(e -> (TypeElement) e)
                    .forEach(element -> {
                        generateAnnotation(createAnnotation(element));
                    });

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return true;
    }

    public void generateAnnotation(AnnotationModel model) {
        try {
            final JavaFileObject file = processingEnv.getFiler().createSourceFile(model.getPackageName() + "." + model.getName());
            VelocityTools.generateFile("annotation.vm", model, file);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public AnnotationModel createAnnotation(TypeElement typeElement) {
        return new AnnotationModel_Builder<>(new AnnotationModel())
                .setPackageName(JwiaTools.getAnnotationPackageName(typeElement, processingEnv))
                .setName(JwiaTools.annotize(typeElement.getSimpleName().toString()))
                .addMethods(createAnnotationMethods(typeElement))
                .addNestedAnnotations(createSubAnnotations(typeElement))
                .addAnnotations(
                        JwiaTools.createSimpleAnnotation(SbapAnnotation.NAME),
                        JwiaTools.createClassLink(typeElement)
                ).get();
    }

    public List<AnnotationModel> createSubAnnotations(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CLASS)
                .map(e -> (TypeElement) e)
                .map(this::createAnnotation)
                .collect(Collectors.toList());
    }

    private List<MethodModel> createAnnotationMethods(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(e -> (VariableElement) e)
                .filter(this::isAnnotationFieldNotIgnored)
                .map(this::createAnnotationMethod)
                .collect(Collectors.toList());
    }

    private MethodModel createAnnotationMethod(VariableElement variableElement) {
        return new MethodModel_Builder<>(new MethodModel())
                .setName(variableElement.getSimpleName().toString())
                .setType(JwiaTools.annotize(variableElement, processingEnv))
                .setDefaultValue(defaultValueForType(variableElement))
                .get();
    }

    public boolean isAnnotationFieldNotIgnored(VariableElement e) {
        return !e.getAnnotationMirrors().stream()
                .anyMatch(a -> {
                    final String annotationName = a.getAnnotationType().toString();
                    return annotationName.equals("sbap.definitions.meta.GeneratedRelationField_A")
                            || annotationName.equals("sbap.definitions.meta.SbapIgnore_A");
                });
    }

    private String defaultValueForType(VariableElement variableElement) {
        final TypeMirror type = variableElement.asType();

        if (type == null) {
            return null;
        }

        final AnnotationMapRule rule = JwiaTools.getAnnotationRuleFor(type.toString());
        if (rule != null && rule.getDefaultValueProcessor() != null) {
            return rule.getDefaultValueProcessor().apply(type);
        } else {
            if (variableElement.asType().getKind() == TypeKind.DECLARED) {
                final TypeElement enumType = (TypeElement) processingEnv.getTypeUtils().asElement(variableElement.asType());

                return enumType.getEnclosedElements().stream()
                        .filter(e -> e.getKind() == ElementKind.ENUM_CONSTANT)
                        .map(e -> (VariableElement) e)
                        .map(f -> variableElement.asType().toString() + "." + f.getSimpleName())
                        .findFirst()
                        .orElse(null);
            }

            return "@" + JwiaTools.annotize(variableElement, processingEnv);
        }

    }

}
