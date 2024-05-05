package com.yunji.titanrtx.bash;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DefaultShell implements Shell {

    private CommandLine  commandLine;

    public DefaultShell(String commander,String options,String arguments){
        this(CommandLine.parse(commander +" "+ options +" "+ arguments));
    }

    public DefaultShell(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    private String doExec(CommandLine commandLine) throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValues(null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
        executor.setWatchdog(watchdog);
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);

        executor.setStreamHandler(streamHandler);
        executor.execute(commandLine);

        String out = outputStream.toString("GBK");
        String error = errorStream.toString("GBK");
        return (out + error).trim();
    }

    @Override
    public String execCommand() throws IOException {
        return  doExec(commandLine);
    }
}
