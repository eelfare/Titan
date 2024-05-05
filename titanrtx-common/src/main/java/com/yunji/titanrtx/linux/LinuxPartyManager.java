package com.yunji.titanrtx.linux;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

/**
 * zjl
 */
public class LinuxPartyManager implements ThritPartyManager {
    private Logger logger = LoggerFactory.getLogger(LinuxPartyManager.class);
    private Connection connection;
    private static final String CHARSET_NAME = "UTF-8";
    private static final Integer SUCCESS = 0;

    @Override
    public boolean login(String host, String user, String pwd) {
        if(host == null || "".equalsIgnoreCase(host.trim())){
            throw new RuntimeException("agent地址不能为空!");
        }
        if(user == null || "".equalsIgnoreCase(user.trim())){
            throw new RuntimeException("用户名不能为空!");
        }
        if(pwd == null || "".equalsIgnoreCase(pwd.trim())){
            throw new RuntimeException("agent地址不能为空!");
        }

        try {
            connection = new Connection(host);
            connection.connect();
            //如果已经认证则不需要二次验证
            if(!connection.isAuthenticationComplete()){
                return connection.authenticateWithPassword(user,pwd);
            }
        } catch (Exception e) {
            throw new RuntimeException("登录失败",e);
        }
        return true;
    }

    @Override
    public void logout() {
        if(connection != null){
            connection.close();
        }
    }

    @Override
    public String executeWithStdout(String cmd, Object... paramters) {
        String cmdStr = MessageFormat.format(cmd,paramters);
        Session session = null;
        try {
            session = connection.openSession();
            session.execCommand(cmdStr, CHARSET_NAME);
            String stdout = trace(session.getStdout());
            String stderr = trace(session.getStderr());
            if(StringUtils.isBlank(stderr) && SUCCESS.equals(session.getExitStatus())){
                return stdout;
            }

            throw new RuntimeException(MessageFormat.format("服务器{0}执行命令{1}失败,result:{2}",connection.getHostname(),cmdStr,stderr));
        } catch (Exception e) {
            throw new RuntimeException(MessageFormat.format("执行{0}失败", cmdStr),e);
        }finally {
            if(session != null){
                session.close();
            }
        }
    }

    private String trace(InputStream std) throws IOException {
        InputStream is = new StreamGobbler(std);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line ;
        while((line = reader.readLine())!=null){
            logger.info(line);
            sb.append(line).append(System.lineSeparator());
        }

        return sb.toString();
    }
}
