package com.yunji.titanrtx.linux;

/**
 * zjl
 */
public interface ThritPartyManager {
    boolean login(String t, String user, String pwd);

    void logout();

    String executeWithStdout(String cmd, Object... paramters);

}
