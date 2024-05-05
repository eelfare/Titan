package com.yunji.titanrtx.bash.support;

import com.yunji.titanrtx.common.u.CommonU;

import java.io.IOException;

/**
 * ping命令用来测试主机之间网络的连通性。执行ping指令会使用ICMP传输协议，发出要求回应的信息，若远端主机的网络功能没有问题，就会回应该信息，因而得知该主机运作正常。
 *
 * 语法
 * ping(选项)(参数)
 * 选项
 * -d：使用Socket的SO_DEBUG功能；
 * -c<完成次数>：设置完成要求回应的次数；
 * -f：极限检测；
 * -i<间隔秒数>：指定收发信息的间隔时间；
 * -I<网络界面>：使用指定的网络界面送出数据包；
 * -l<前置载入>：设置在送出要求信息之前，先行发出的数据包；
 * -n：只输出数值；
 * -p<范本样式>：设置填满数据包的范本样式；
 * -q：不显示指令执行过程，开头和结尾的相关信息除外；
 * -r：忽略普通的Routing Table，直接将数据包送到远端主机上；
 * -R：记录路由过程；
 * -s<数据包大小>：设置数据包的大小；
 * -t<存活数值>：设置存活数值TTL的大小；
 * -v：详细显示指令的执行过程。
 *
 */
public class PingShell extends AbstractShell {

    private static final String COMMANDER = "ping";

    public PingShell(String options,String arguments) {
        super(COMMANDER,options,arguments);
    }

    public double pingExpend() throws IOException {
        if (CommonU.istLinux())return 0;
        String content = execCommand();
        String[] lineContent = content.split("\n");
        String targetLine = null;
        for (String line : lineContent){
            if (line.contains("rtt")){
                targetLine = line;
                break;
            }
        }
        if (targetLine != null){
            String[] pair = targetLine.split("=");
            String[] expend = pair[1].trim().split("/");
            return Double.valueOf(expend[1]);
        }
        return 0;
    }

}
