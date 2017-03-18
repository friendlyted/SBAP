package ru.sbsoft.sbap.plugin.apt.jwi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import ru.sbsoft.sbap.plugin.apt.jwia.JwiaTools;
import ru.sbsoft.sbap.plugin.apt.jwia.SbapAnnotationVisitor;
import ru.sbsoft.sbap.plugin.apt.jwia.VelocityTools;
import ru.sbsoft.sbap.plugin.apt.jwia.model.ArgumentModel;
import ru.sbsoft.sbap.plugin.apt.jwia.model.MethodModel;
import ru.sbsoft.sbap.plugin.apt.jwia.model.MethodModel_Builder;

import ru.sbsoft.sbap.plugin.apt.jwi.model.InterfaceModel;
import ru.sbsoft.sbap.plugin.apt.jwi.model.InterfaceModel_Builder;
import ru.sbsoft.sbap.plugin.apt.jwia.model.ArgumentModel_Builder;
import ru.sbsoft.sbap.system.annotation.SbapInterface;
import sbap.definitions.meta.HasActions_A;
import sbap.definitions.meta.SbapContext_A;
import sbap.definitions.meta.SbapIgnore_A;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@SupportedAnnotationTypes("javax.annotation.Generated")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JwiProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            final Set<? extends Element> generatedList = roundEnv.getElementsAnnotatedWith(Generated.class);

            generatedList.stream()
                    .filter(e -> e.getKind() == ElementKind.CLASS && e.getEnclosingElement().getKind() == ElementKind.PACKAGE)
                    .map(e -> (TypeElement) e)
                    .forEach(element -> {
                        final InterfaceModel interfaceModel = createInterface(element);
                        reachModelWithMeta(interfaceModel, element);
                        generateInterface(interfaceModel);
                    });

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return true;
    }

    public void generateInterface(InterfaceModel model) {
        try {
            final JavaFileObject file = processingEnv.getFiler().createSourceFile(model.getPackageName() + "." + model.getName());
            VelocityTools.generateFile("interface.vm", model, file);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public InterfaceModel createInterface(TypeElement typeElement) {
        final InterfaceModel iface = new InterfaceModel_Builder<>(new InterfaceModel())
                .setPackageName(JwiaTools.getInterfacePackageName(typeElement, processingEnv))
                .setName(JwiaTools.interfaze(typeElement.getSimpleName().toString()))
                .addMethods(createInterfaceMethods(typeElement))
                .addNestedInterfaces(createSubInterfaces(typeElement))
                .setExtendsName(extractInterfaceExtendsName(typeElement))
                .addAnnotations(
                        JwiaTools.createSimpleAnnotation(SbapInterface.NAME),
                        JwiaTools.createClassLink(typeElement)
                ).get();

        if (typeElement.getAnnotation(SbapContext_A.class) != null) {
            iface.getAnnotations().add(JwiaTools.createSimpleAnnotation(SbapContext_A.FULL_NAME));
        }
        return iface;
    }

    public List<InterfaceModel> createSubInterfaces(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CLASS)
                .map(e -> (TypeElement) e)
                .map(this::createInterface)
                .collect(Collectors.toList());
    }

    public List<MethodModel> createInterfaceMethods(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .filter(e -> !e.getModifiers().contains(Modifier.STATIC))
                .map(e -> (VariableElement) e)
                .filter(this::isInterfaceFieldNotIgnored)
                .flatMap(e -> createGetterAndSetterFor(e).stream())
                .collect(Collectors.toList());
    }

    public List<MethodModel> createGetterAndSetterFor(VariableElement fieldElement) {
        final List<MethodModel> result = new ArrayList<>();
        final String name = fieldElement.getSimpleName().toString().replaceFirst("^_+", "");
        final String capName = capitalize(name);
        final String type = JwiaTools.interfaze(fieldElement, processingEnv);

        result.add(new MethodModel_Builder<>(new MethodModel())
                .setName("set" + capName)
                .addArguments(new ArgumentModel_Builder<>(new ArgumentModel())
                        .setName(name)
                        .setType(type)
                        .get()
                )
                .get());

        result.add(new MethodModel_Builder<>(new MethodModel())
                .setName("get" + capName)
                .setType(type)
                .get());

        return result;
    }

    public ArgumentModel createMethodArgument(VariableElement variableElement) {
        return new ArgumentModel_Builder<>(new ArgumentModel())
                .setName(variableElement.getSimpleName().toString())
                .setType(JwiaTools.interfaze(variableElement, processingEnv))
                .get();
    }

    public boolean isInterfaceFieldNotIgnored(final VariableElement field) {
        if (field == null) {
            return true;
        }

        return field.getAnnotation(SbapIgnore_A.class) == null;
    }

    private void reachModelWithMeta(InterfaceModel interfaceModel, TypeElement typeElement) {
        final SbapAnnotationVisitor visitor = new SbapAnnotationVisitor();

        typeElement.getAnnotationMirrors().forEach(am -> {
            if (am.getAnnotationType().toString().equals(HasActions_A.FULL_NAME)) {
                final Map<String, Object> result = visitor.visitAnnotation(am, null);
                if (result.containsKey("actions") && result.get("actions") != null) {
                    interfaceModel.setActionList((List<Map<String, Object>>) result.get("actions"));
                }
            }
        });

    }

    private String extractInterfaceExtendsName(TypeElement typeElement) {
        final TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.toString().equals(Object.class.getName())) {
            return null;
        }
        return JwiaTools.interfaze(superclass, processingEnv);
    }

    private static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

}
