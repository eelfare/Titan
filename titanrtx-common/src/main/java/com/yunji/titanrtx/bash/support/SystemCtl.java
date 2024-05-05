package com.yunji.titanrtx.bash.support;


/**
 * 自己去网上查怎么使用吧
 *
 */
public class SystemCtl extends AbstractShell{

    private static final String COMMANDER = "systemctl";

    public SystemCtl(String options, String arguments) {
        super(COMMANDER, options, arguments);
    }

}
