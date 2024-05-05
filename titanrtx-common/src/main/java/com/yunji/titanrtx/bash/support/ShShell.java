package com.yunji.titanrtx.bash.support;

public class ShShell extends AbstractShell {

    private static final String COMMANDER = "sh";

    public ShShell(String options, String arguments) {
        super(COMMANDER, options, arguments);
    }
}
