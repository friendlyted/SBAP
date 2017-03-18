package ru.sbsoft.sbap.plugin.apt.jwia;

import java.io.IOException;
import java.io.Writer;
import javax.tools.JavaFileObject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class VelocityTools {

    private VelocityTools() {
    }

    private final static VelocityEngine VE = new VelocityEngine();

    static {
        VE.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    }

    public static void generateFile(String template, Object model, JavaFileObject classFile) {
        try {
            final Template t = VE.getTemplate(template);
            final VelocityContext vc = new VelocityContext();
            vc.put("model", model);
            try (Writer writer = classFile.openWriter()) {
                t.merge(vc, writer);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
