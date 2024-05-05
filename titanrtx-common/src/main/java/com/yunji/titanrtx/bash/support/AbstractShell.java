package com.yunji.titanrtx.bash.support;

import com.yunji.titanrtx.bash.DefaultShell;
import com.yunji.titanrtx.bash.Shell;

import java.io.IOException;

public abstract class AbstractShell implements Shell {

    private String command;

    private String options;

    private String arguments;

    AbstractShell(String command, String options, String arguments) {
        this.command = command;
        this.options = options;
        this.arguments = arguments;
    }

    @Override
    public String execCommand() throws IOException {
        replaceNullToBlank();
        return new DefaultShell(command,options,arguments).execCommand();
    }

    private void replaceNullToBlank() {
        if (options == null)options = "";
        if (arguments == null)arguments="";
    }

}
