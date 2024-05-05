package com.yunji.titanrtx.manager.web.support;

import java.util.ArrayList;
import java.util.List;

public class Constant {
    public static final List<String> ROOT = new ArrayList<>();
    public static final String USER_LOGIN_SESSION = "user_login_session";
    public static final String SSO_COOKIE_NAME = "JESSIONID";

    static {
        // 景风
        ROOT.add("2850");
        // 丹宁
        //ROOT.add("2595");
        // 青老板
        ROOT.add("0185");
        // 老潮
        ROOT.add("2029");
    }
}
