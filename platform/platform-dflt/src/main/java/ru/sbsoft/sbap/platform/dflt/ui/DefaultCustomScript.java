package ru.sbsoft.sbap.platform.dflt.ui;

import sbap.definitions.application.TCustomScript_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class DefaultCustomScript implements TCustomScript_I {

    public String script;

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public void setScript(String value) {
        this.script = value;
    }

}
