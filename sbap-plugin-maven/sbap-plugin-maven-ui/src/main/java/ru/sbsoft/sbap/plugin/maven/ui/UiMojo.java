package ru.sbsoft.sbap.plugin.maven.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import ru.sbsoft.sbap.plugin.maven.ui.model.BuilderMethodModel;
import ru.sbsoft.sbap.plugin.maven.ui.model.BuilderModel;
import ru.sbsoft.sbap.plugin.maven.ui.model.ElementFactoryModel;
import ru.sbsoft.sbap.plugin.maven.ui.model.RegisterModel;
import ru.sbsoft.sbap.plugin.maven.ui.model.RegistersModel;
import ru.sbsoft.sbap.schema.reader.ApplicationReadException;
import ru.sbsoft.sbap.schema.reader.ApplicationReader;
import ru.sbsoft.sbap.schema.reader.model.ApplicationContext;
import ru.sbsoft.sbap.schema.reader.model.SettingsModel;
import ru.sbsoft.sbap.schema.reader.model.SettingsModel_Builder;
import ru.sbsoft.sbap.system.utils.SbapReaderUtils;
import sbap.definitions.application.TApplication;
import sbap.definitions.application.TForm_I;
import sbap.definitions.application.TTree_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
@Mojo(
        name = "ui",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        threadSafe = true
)
public class UiMojo extends AbstractMojo {

    private final static VelocityEngine VE = new VelocityEngine();

    static {
        VE.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    }

    @Parameter(defaultValue = "${basedir}/src/main/resources")
    private String basedir;

    @Parameter
    private List<String> trees;

    @Parameter
    private List<String> forms;

    @Parameter
    private String application;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/ui")
    private File output;

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    private FieldsResolver fieldsResolver;
    private BuilderGenerator builderGenerator;

    public String getBasedir() {
        return basedir;
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public List<String> getTrees() {
        return trees;
    }

    public void setTrees(List<String> trees) {
        this.trees = trees;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public FieldsResolver getFieldsResolver() {
        if (fieldsResolver == null) {
            fieldsResolver = new FieldsResolver();
        }
        return fieldsResolver;
    }

    public void setFieldsResolver(FieldsResolver fieldsResolver) {
        this.fieldsResolver = fieldsResolver;
    }

    public BuilderGenerator getBuilderGenerator() {
        if (builderGenerator == null) {
            builderGenerator = new BuilderGenerator();
        }
        return builderGenerator;
    }

    public void setBuilderGenerator(BuilderGenerator builderGenerator) {
        this.builderGenerator = builderGenerator;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        project.addCompileSourceRoot(output.getAbsolutePath());
//        final File applicationListFile = new File(output, "META-INF/services/ru.sbsoft.sbap.application");

        final ApplicationContext applicationContext;

        try {
            applicationContext = new ApplicationReader().readApplication(
                    new SettingsModel_Builder(new SettingsModel())
                            .setBasedir(basedir)
                            .setApplication(application)
                            .setTrees(trees)
                            .setForms(forms)
                            .get()
            );
        } catch (ApplicationReadException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("ApplicationReader error", ex);
        }

        try {
            final TApplication app = applicationContext.getApplication();

            final ElementFactoryModel appModel = createElementModel(applicationContext, app);
            appModel.setPackageName(app.getDefaultPackage());
            appModel.setClassName(app.getName());
            appModel.getCreateMethod().setFactoryMethod(getFactoryMethodFor(appModel.getCreateMethod().getReturnType()));
            createElement(appModel);

            createElementPack(applicationContext, "TreeRegister", app.getDefaultPackage(), applicationContext.getTreeList(), TTree_I.class);
            createElementPack(applicationContext, "FormRegister", app.getDefaultPackage(), applicationContext.getFormList(), TForm_I.class);

            final RegistersModel registersModel = new RegistersModel();
            registersModel.setPackageName(app.getDefaultPackage());
            createRegisters(registersModel);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("ApplicationReader error", ex);
        }
    }

    private static String toFile(String pkg, String className) {
        return pkg.replaceAll("\\.", "/") + "/" + className + ".java";
    }

    public <T> void createElementPack(ApplicationContext applicationContext, String registerName, String defaultPackage, List<T> list, Class<?> registerGeneric) throws MojoExecutionException {
        final RegisterModel treeRegisterModel = new RegisterModel();
        treeRegisterModel.setClassName(registerName);
        treeRegisterModel.setPackageName(defaultPackage);
        treeRegisterModel.setType(registerGeneric.getName());

        for (T item : list) {
            final String name = getFieldsResolver().resolveName(item);
            final ElementFactoryModel elementModel = createElementModel(applicationContext, item);
            elementModel.setPackageName(defaultPackage);
            elementModel.setClassName(name);
            elementModel.getCreateMethod().setFactoryMethod(getFactoryMethodFor(elementModel.getCreateMethod().getReturnType()));
            createElement(elementModel);
            treeRegisterModel.addFactory(name, elementModel.getPackageName() + "." + elementModel.getClassName());
        }

        createRegister(treeRegisterModel);
    }

    public ElementFactoryModel createElementModel(ApplicationContext applicationContext, Object object) {
        final String createPrefix = "create_";
        final ElementFactoryModel result = new ElementFactoryModel();
        final List<Object> objectsToCreate = new ArrayList<>();
        final BuilderMethodModel builderMethodModel = new BuilderMethodModel();
        builderMethodModel.setBuilder(getBuilderGenerator().generateBuilderFor(object, o -> {
            objectsToCreate.add(o);
            return createPrefix + getFieldsResolver().resolveName(o);
        }));
        builderMethodModel.setReturnType(getBuilderGenerator().ifaceFor(object).getName());
        result.setCreateMethod(builderMethodModel);
        final List<BuilderMethodModel> methodList = new ArrayList<>();
        final List<Object> anotherObjectsToCreate = new ArrayList<>();

        while (!objectsToCreate.isEmpty()) {
            objectsToCreate.forEach(obj -> {
                final String iType = SbapReaderUtils.getInterfaceClassFor(obj.getClass()).getName();
                final String returnType = iType.replaceAll("\\$", ".");
                final boolean isRegistered = getFieldsResolver().isRegistered(obj);
                final String name = getFieldsResolver().resolveName(obj);

                final BuilderMethodModel method = new BuilderMethodModel();
                method.setName(createPrefix + name);
                method.setReturnType(returnType);
                if (!isRegistered) {
                    method.setFactoryMethod(getFactoryMethodFor(iType.replaceAll("\\$", "_")));
                    method.setBuilder(getBuilderGenerator().generateBuilderFor(obj, o -> {
                        anotherObjectsToCreate.add(o);
                        return createPrefix + getFieldsResolver().resolveName(o);
                    }));
                } else {
                    method.setFactoryMethod(getRegisteredMethodFor(iType.replaceAll("\\$", "_"), name, applicationContext.getApplication().getDefaultPackage()));
                    method.setBuilder(new BuilderModel(getBuilderGenerator().builderNameFor(iType.replaceAll("\\$", "_")), Collections.EMPTY_LIST));
                }
                methodList.add(method);
            });
            objectsToCreate.clear();
            objectsToCreate.addAll(anotherObjectsToCreate);
            anotherObjectsToCreate.clear();
        }

        result.getFactoryMethods().addAll(methodList);
        return result;
    }

    public void createElement(ElementFactoryModel elementModel) throws MojoExecutionException {
        try {
            final File genFile = new File(output, toFile(elementModel.getPackageName(), elementModel.getClassName() + "_Gen"));
            genFile.getParentFile().mkdirs();
            final Template genTpl = VE.getTemplate("element_factory.vm");
            final VelocityContext genCtx = new VelocityContext();
            genCtx.put("element", elementModel);
            try (Writer writer = new FileWriter(genFile)) {
                genTpl.merge(genCtx, writer);
            }

            if (!classExists(elementModel.getPackageName() + File.separator + elementModel.getClassName())) {

                final File implFile = new File(output, toFile(elementModel.getPackageName(), elementModel.getClassName()));
                final Template implTpl = VE.getTemplate("impl.vm");
                final VelocityContext implCtx = new VelocityContext();
                implCtx.put("packageName", elementModel.getPackageName());
                implCtx.put("className", elementModel.getClassName());
                try (Writer writer = new FileWriter(implFile)) {
                    implTpl.merge(implCtx, writer);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("", ex);
        }
    }

    public void createRegister(RegisterModel registerModel) throws MojoExecutionException {
        try {
            final File treeFactoryFile = new File(output, toFile(registerModel.getPackageName(), registerModel.getClassName()));
            treeFactoryFile.getParentFile().mkdirs();
            final Template t = VE.getTemplate("register.vm");
            final VelocityContext vc = new VelocityContext();
            vc.put("register", registerModel);
            try (Writer writer = new FileWriter(treeFactoryFile)) {
                t.merge(vc, writer);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("", ex);
        }
    }

    public void createRegisters(RegistersModel registersModel) throws MojoExecutionException {
        try {
            final File treeFactoryFile = new File(output, toFile(registersModel.getPackageName(), "Registers"));
            treeFactoryFile.getParentFile().mkdirs();
            final Template t = VE.getTemplate("registers.vm");
            final VelocityContext vc = new VelocityContext();
            vc.put("registers", registersModel);
            try (Writer writer = new FileWriter(treeFactoryFile)) {
                t.merge(vc, writer);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("", ex);
        }
    }

    public String getFactoryMethodFor(String type) {
        final String typeName = type.replaceAll(".*\\.([A-Z_][a-zA-Z0-9_]+)_I(?:_Builder)?", "$1");
        final String methodName = typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
        return "factoryContext.getUiFactory()." + methodName + "()";
    }

    private String getRegisteredMethodFor(String returnType, String name, String defaultPackage) {
        final String registerName;
        switch (returnType) {
            case "sbap.definitions.application.TTree_I":
                registerName = defaultPackage + ".Registers.treeRegister()";
                break;
            default:
                registerName = "";
        }

        return registerName + ".get(\"" + name + "\", factoryContext)";
    }

    private boolean classExists(String className) {
        final List<String> sources = project.getCompileSourceRoots();
        for (String source : sources) {
            final File srcDir = new File(source);
            final File classFile = new File(srcDir, className.replaceAll("\\.", "/") + ".java");
            if (classFile.exists()) {
                return true;
            }
        }
        return false;
    }
}
