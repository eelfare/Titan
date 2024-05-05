package com.yunji.titanrtx.bash.support;

public class CurlShell extends AbstractShell {

    private static final String COMMANDER = "curl";

    public CurlShell(String options,String arguments) {
        super(COMMANDER,options,arguments);
    }


}
