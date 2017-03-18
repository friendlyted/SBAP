package ru.sbsoft.sbap.builder.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import ru.sbsoft.sbap.builder.generator.api.Defaults;
import ru.sbsoft.sbap.builder.generator.api.GenerateBuilder;

/**
 *
 * @author Fedor Resnyanskiy
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes(GenerateBuilder.GenerateBuilderName)
public class BuilderGenerator extends AbstractProcessor {

    private final static VelocityEngine VE = new VelocityEngine();

    static {
        VE.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    }

    @Override
    synchronized public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        final Set<? extends Element> generateBuilderElements = roundEnv.getElementsAnnotatedWith(GenerateBuilder.class);
        for (Element elementNeedsBuilder : generateBuilderElements) {
            final BuilderModel builderModel = createBuilderModeFromAnnotatedElement((TypeElement) elementNeedsBuilder);
            generateBuilder(builderModel);
        }

        return true;
    }

    private BuilderModel createBuilderModeFromAnnotatedElement(TypeElement element) {
        final GenerateBuilder gb = element.getAnnotation(GenerateBuilder.class);
        final BuilderModel model = new BuilderModel();

        model.setTargetClassName(element.asType().toString());
        model.setIface(element.getKind().isInterface());
        model.setName(toBuilderName(element, gb.name()));
        model.setPackageName(toBuilderPackage(element, gb.packageName()));
        model.setMethods(grabMethodsFrom(element));

        return model;
    }

    private List<MethodModel> grabMethodsFrom(TypeElement element) {
        final List<MethodModel> improvedMethods = new ArrayList<>();
        final List<MethodModel> methodList = processingEnv.getElementUtils().getAllMembers(element).stream().sequential()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .filter(e -> e.getSimpleName().toString().startsWith("set"))
                .map(e -> {
                    if (e.getParameters().size() == 1 && e.getParameters().get(0).asType().toString().startsWith("java.util.List")) {
                        improvedMethods.addAll(improve(e));
                    }
                    final MethodModel model = new MethodModel();
                    model.setName(e.getSimpleName().toString());
                    model.setParametersWithTypes(parametersFrom(e, true));
                    model.setParametersWithoutTypes(parametersFrom(e, false));
                    return model;
                })
                .collect(Collectors.toList());

        methodList.addAll(improvedMethods);
        return methodList;
    }

    private void generateBuilder(BuilderModel builderModel) {
        try {
            final JavaFileObject builderClassFile = processingEnv.getFiler().createSourceFile(builderModel.getPackageName() + "." + builderModel.getName());
            final Template t = VE.getTemplate("builder.vm");
            final VelocityContext vc = new VelocityContext();
            vc.put("model", builderModel);
            final Writer writer = builderClassFile.openWriter();
            t.merge(vc, writer);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(BuilderGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String parametersFrom(ExecutableElement e, boolean withTypes) {
        final StringBuilder sb = new StringBuilder();

        for (VariableElement parameter : e.getParameters()) {
            if (withTypes) {
                sb.append(parameter.asType().toString()).append(" ");
            }
            sb.append(parameter.getSimpleName().toString()).append(", ");
        }

        return sb.toString().substring(0, sb.length() - 2);
    }

    private List<MethodModel> improve(ExecutableElement e) {
        final List<MethodModel> result = new ArrayList<>();

        final String parameter = e.getParameters().get(0).asType().toString().replaceAll("java.util.List<(.*)>", "$1");
        final MethodModel singleModel = new MethodModel();
        final String parameterName = e.getSimpleName().toString().replaceAll("^set|List$|es$|s$", "");
        singleModel.setName("add" + parameterName);
        singleModel.setParametersWithTypes(parameter + " " + parameterName);
        singleModel.setAction("target." + e.getSimpleName().toString().replaceAll("^set", "get") + "().add(" + parameterName + ");");
        result.add(singleModel);

        final MethodModel arrayModel = new MethodModel();
        arrayModel.setName(e.getSimpleName().toString().replaceAll("^set", "add"));
        arrayModel.setParametersWithTypes(parameter + "... " + parameterName);
        arrayModel.setAction("target." + e.getSimpleName().toString().replaceAll("^set", "get") + "().addAll(java.util.Arrays.asList(" + parameterName + "));");
        result.add(arrayModel);

        final MethodModel listModel = new MethodModel();
        listModel.setName(e.getSimpleName().toString().replaceAll("^set", "add"));
        listModel.setParametersWithTypes(e.getParameters().get(0).asType().toString() + " " + parameterName);
        listModel.setAction("target." + e.getSimpleName().toString().replaceAll("^set", "get") + "().addAll(" + parameterName + ");");
        result.add(listModel);

        return result;
    }

    private String toBuilderName(TypeElement element, String name) {
        if (!name.equals(Defaults.DEFAULT_STRING)) {
            return name;
        }

        StringBuilder sb = new StringBuilder(element.getSimpleName()).append("_Builder");

        Element parent = element.getEnclosingElement();
        while (parent.getKind() != ElementKind.PACKAGE) {
            sb.insert(0, parent.getSimpleName() + "_");
            parent = parent.getEnclosingElement();
        }

        return sb.toString();
    }

    private String toBuilderPackage(TypeElement element, String packageName) {
        if (!packageName.equals(Defaults.DEFAULT_STRING)) {
            return packageName;
        }

        Element parent = element.getEnclosingElement();
        while (parent.getKind() != ElementKind.PACKAGE) {
            parent = parent.getEnclosingElement();
        }
        return parent.asType().toString();
    }

}
