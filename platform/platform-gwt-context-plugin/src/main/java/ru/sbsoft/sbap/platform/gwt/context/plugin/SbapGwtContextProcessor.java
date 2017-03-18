package ru.sbsoft.sbap.platform.gwt.context.plugin;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import ru.sbsoft.sbap.platform.gwt.context.plugin.model.ArgumentModel;
import ru.sbsoft.sbap.platform.gwt.context.plugin.model.ArgumentModel_Builder;
import ru.sbsoft.sbap.platform.gwt.context.plugin.model.ContextObjectModel;
import ru.sbsoft.sbap.platform.gwt.context.plugin.model.ContextObjectModel_Builder;
import ru.sbsoft.sbap.platform.gwt.context.plugin.model.MethodModel;
import ru.sbsoft.sbap.platform.gwt.context.plugin.model.MethodModel_Builder;
import ru.sbsoft.sbap.system.annotation.SbapInterface;
import sbap.definitions.meta.SbapContext_A;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(SbapContext_A.FULL_NAME)
public class SbapGwtContextProcessor extends AbstractProcessor {

    private final static VelocityEngine VE = new VelocityEngine();

    static {
        VE.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        try {
            final List<ContextObjectModel> modelList = roundEnv.getElementsAnnotatedWith(SbapContext_A.class).stream()
                    .filter(e -> {
                        return e.getKind() == ElementKind.INTERFACE;
                    })
                    .map(e -> (TypeElement) e)
                    .peek(this::print)
                    .map(this::readElement)
                    .filter(m -> m != null)
                    .collect(Collectors.toList());

            generateSourcesFor(modelList);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private ContextObjectModel readElement(TypeElement ifaceElement) {
        if (ifaceElement == null) {
            return null;
        }

        return new ContextObjectModel_Builder<>(new ContextObjectModel())
                .setName(ifaceElement.getSimpleName().toString())
                .setClassName(ifaceElement.asType().toString().replaceAll("<.*", ""))
                .addMethodList(extractMethodsToAccess(ifaceElement))
                .get();
    }

    private void generateSourcesFor(List<ContextObjectModel> modelList) {
        try {
            final JavaFileObject converterFile = processingEnv.getFiler().createSourceFile("ru.sbsoft.sbap.platform.gxt.context.ContextConverter");

            final Template t = VE.getTemplate("context_converter.vm");
            final VelocityContext vc = new VelocityContext();
            vc.put("modelList", modelList);
            try (Writer writer = converterFile.openWriter()) {
                t.merge(vc, writer);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("", ex);
        }
    }

    private List<MethodModel> extractMethodsToAccess(TypeElement interfaceElement) {
        return interfaceElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .map(method -> buildMethod(method))
                .collect(Collectors.toList());
    }

    private MethodModel buildMethod(ExecutableElement method) {
        return new MethodModel_Builder<>(new MethodModel())
                .setName(method.getSimpleName().toString())
                .addArgList(extractArguments(method))
                .get();
    }

    private List<ArgumentModel> extractArguments(ExecutableElement method) {
        return method.getParameters().stream()
                .map(p -> buildArgument(p))
                .collect(Collectors.toList());

    }

    private ArgumentModel buildArgument(VariableElement ve) {
        return new ArgumentModel_Builder<>(new ArgumentModel())
                .setName(ve.getSimpleName().toString())
                .setType(convertType(ve.asType()))
                .get();
    }

    private String convertType(TypeMirror type) {

        switch (type.getKind()) {
            case BOOLEAN:
                return "Z";
            case BYTE:
                return "B";
            case CHAR:
                return "C";
            case SHORT:
                return "S";
            case INT:
                return "I";
            case FLOAT:
                return "F";
            case DOUBLE:
                return "D";
            case ARRAY:
                return "[" + type.toString().replaceAll("([a-z0-9_]+)\\.", "$1/").replaceAll("([A-Z][a-z0-9_]*)(/|\\.)", "$1\\$").replaceAll("<.*", "");
            default:
                return "L" + type.toString().replaceAll("([a-z0-9_]+)\\.", "$1/").replaceAll("([A-Z][a-z0-9_]*)(/|\\.)", "$1\\$").replaceAll("<.*", "") + ";";
        }
    }

    private TypeElement extractSbapInterfaceFrom(TypeElement typeElement) {
        final Optional<? extends AnnotationMirror> sbapInterfaceAnnotationOption = typeElement.getAnnotationMirrors().stream()
                .filter(a -> a.getAnnotationType().asElement().asType().toString().equals(SbapInterface.FULL_NAME))
                .findFirst();

        if (!sbapInterfaceAnnotationOption.isPresent()) {
            return typeElement;
        }

        final AnnotationMirror sbapInterfaceAnnotation = sbapInterfaceAnnotationOption.get();
        final Optional<TypeElement> ifaceName = sbapInterfaceAnnotation.getElementValues().entrySet().stream()
                .filter(e -> {
                    return e.getKey().getSimpleName().toString().equals("value");
                })
                .findFirst()
                .map(e -> e.getValue().toString())
                .map(name -> {
                    return processingEnv.getElementUtils().getTypeElement(name.substring(0, name.length() - 6));
                });
        if (!ifaceName.isPresent()) {
            return null;
        }

        return ifaceName.get();
    }

    private void print(Element e) {
        System.err.println("GOT e: " + e);
    }

}
