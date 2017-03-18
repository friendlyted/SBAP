package ru.sbsoft.sbap.plugin.xjc.names;

import com.sun.codemodel.*;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.Outline;
import java.util.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import ru.sbsoft.sbap.names.FullNameIs;
import static ru.sbsoft.sbap.names.NameConverter.convertFieldNameToStaticName;
import ru.sbsoft.sbap.names.NameIs;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class NamesPlugin extends Plugin {

    private static final int PSF = JMod.PUBLIC | JMod.STATIC | JMod.FINAL;

    private static class FullNameExpr extends JExpressionImpl {

        private final JType type;
        private final JFieldVar field;

        public FullNameExpr(JType type, JFieldVar field) {
            this.type = type;
            this.field = field;
        }

        @Override
        public void generate(JFormatter f) {
            f.p(type.fullName() + "." + field.name());
        }
    }

    @Override
    public String getOptionName() {
        return "Xnames";
    }

    @Override
    public String getUsage() {
        return "-Xnames : creating validating annotations for classes and fields";
    }

    @Override
    public boolean run(final Outline outline, final Options opt, final ErrorHandler errorHandler) throws SAXException {

        outline.getClasses().stream()
                //.filter(c -> c.ref.)
                .forEach(c -> {
                    final JDefinedClass ref = c.ref;
                    final JFieldVar nameField = ref.field(PSF, String.class, "NAME", JExpr.lit(ref.name()));
                    nameField.javadoc().add("Name of current class");
                    ref.annotate(NameIs.class).param("value", new FullNameExpr(ref, nameField));
                    final JFieldVar fullNameField = ref.field(PSF, String.class, "FULL_NAME", JExpr.lit(ref.fullName()));
                    fullNameField.javadoc().add("Full name of current class");
                    ref.annotate(FullNameIs.class).param("value", new FullNameExpr(ref, fullNameField));

                    new ArrayList<>(ref.fields().values()).stream()
                            .filter(f -> (f.mods().getValue() & JMod.STATIC) == 0)
                            .forEach(f -> {
                                final String fieldName = convertFieldNameToStaticName(f.name());
                                final JExpression init = JExpr.lit(f.name());
                                final JFieldVar fieldNameField = ref.field(PSF, String.class, fieldName, init);
                                fieldNameField.javadoc().add("Name of field '" + f.name() + "'");
                                f.annotate(NameIs.class).param("value", fieldNameField);
                            });
                });

        return true;

    }

}
