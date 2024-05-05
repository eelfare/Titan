package com.yunji.titanrtx.plugin.monitor.enums;

import lombok.Getter;

@Getter
public enum VCM {


    /**
     * base
     */
    lanOuttraffic("内网出带宽","Mbps","unInstanceId"),
    lanIntraffic("内网入带宽","Mbps","unInstanceId"),
    lanOutpkg("内网出包量","个/s","unInstanceId"),
    lanInpkg("内网入包量","个/s","unInstanceId"),
    WanOuttraffic("外网出带宽","Mbps","unInstanceId"),
    WanIntraffic("外网入带宽","Mbps","unInstanceId"),
    WanOutpkg("外网出包量","个/s","unInstanceId"),
    WanInpkg("外网入包量","t个/s","unInstanceId"),

    /**
     * 安装监控agent才能获取数据的监控指标
     */
    CPUUsage("CPU利用率","%","unInstanceId"),
    CPULoadAvg("CPU平均负载","-","unInstanceId"),
    MemUsed("内存使用量","MB","unInstanceId"),
    MemUsage("内存利用率","%","unInstanceId"),
    TcpCurrEstab("TCP 连接数","个","unInstanceId");


    private String describe;
    private String units;
    private String dimensionality;

    VCM(String describe, String units, String dimensionality) {
        this.describe = describe;
        this.units = units;
        this.dimensionality = dimensionality;
    }

}
