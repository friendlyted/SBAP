package ru.sbsoft.sbap.plugin.xjc.types;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.Outline;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class TypesPlugin extends Plugin {

    @Override
    public String getOptionName() {
        return "Xtypes";
    }

    @Override
    public String getUsage() {
        return "-Xtypes : mapping types";
    }

    @Override
    public void onActivated(Options opts) throws BadCommandLineException {
        opts.addBindFile(new InputSource("jar:" + getClass().getResource("types.xjb").getFile()));
    }

    @Override
    public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) throws SAXException {
        return true;
    }

}
