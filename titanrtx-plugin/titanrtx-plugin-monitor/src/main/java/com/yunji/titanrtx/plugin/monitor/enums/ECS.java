package com.yunji.titanrtx.plugin.monitor.enums;

import lombok.Getter;


@Getter
public enum  ECS {

    /*
    *
    * 基础监控项
    *    ECS基础监控数据，无需安装插件即可查询监控数据。
    *    Project为acs_ecs_dashboard，采样周期为60s，Period赋值为60或60的整数倍。
    *    Dimensions中的instanceId赋值ecs实例的instanceId
    * */
    cpuUtilization("CPU百分比","Percent","instanceId","Average、Minimum、Maximum"),
    internetInRate("公网流入带宽","bit/s","instanceId","Average、Minimum、Maximum"),
    intranetInRate("私网流入带宽","bit/s","instanceId","Average、Minimum、Maximum"),
    internetOutRate("公网流出带宽","bit/s","instanceId","Average、Minimum、Maximum"),
    intranetOutRate("私网流出带宽","bit/s","instanceId","Average、Minimum、Maximum"),
    internetOutRate_percent("公网流出带宽使用率","%","instanceId","Average"),
    diskReadBps("系统磁盘总读BPS","Bps","instanceId","Average、Minimum、Maximum"),
    diskWriteBps("系统磁盘总写BPS","Bps","instanceId","Average、Minimum、Maximum"),
    diskReadIOps("系统磁盘读IOPS","Count/Second","instanceId","Average、Minimum、Maximum"),
    diskWriteIOps("系统磁盘写IOPS","Count/Second","instanceId","Average、Minimum、Maximum"),
    vpc_publicIp_internetInRate("专有网络公网流入带宽","bit/s","instanceId","Average、Minimum、Maximum"),
    vpc_publicIp_internetOutRate("专有网络公网流出带宽","bit/s","instanceId","Average、Minimum、Maximum"),
    vpc_publicIp_internetOutRate_percent("专有网络公网流出带宽使用率","%","instanceId","Average"),
    /*
    * 操作系统级别监控项
    *  period最小为15秒
    * */
    cpu_idle("Host.cpu.idle，当前空闲CPU百分比","%","instanceId","Average、Minimum、Maximum"),
    cpu_system("Host.cpu.system，当前内核空间占用CPU百分比","%","instanceId","Average、Minimum、Maximum"),
    cpu_user("Host.cpu.user，当前用户空间占用CPU百分比","%","instanceId","Average、Minimum、Maximum"),
    cpu_wait("Host.cpu.iowait，当前等待IO操作的CPU百分比","%","instanceId","Average、Minimum、Maximum"),
    cpu_other("Host.cpu.other，其他占用CUP百分比，其他消耗，计算方式为（Nice + SoftIrq + Irq + Stolen）的消耗","%","instanceId","Average、Minimum、Maximum"),
    cpu_total("Host.cpu.total，当前消耗的总CPU百分比","%","instanceId","Average、Minimum、Maximum"),

    memory_totalspace("Host.mem.total，内存总量","byte","instanceId","Average、Minimum、Maximum"),
    memory_usedspace("Host.mem.used，已用内存量 ，用户程序使用的内存 + buffers + cached，buffers为缓冲区占用的内存空间，cached为系统缓存占用的内存空间","byte","instanceId","Average、Minimum、Maximum"),
    memory_actualusedspace("Host.mem.actualused，用户实际使用的内存，计算方法为（used - buffers - cached）","byte","instanceId","Average、Minimum、Maximum"),
    memory_freespace("Host.mem.free，剩余内存量","byte","instanceId","Average、Minimum、Maximum"),
    memory_freeutilization("Host.mem.freeutilization， 剩余内存百分比","%","instanceId","Average、Minimum、Maximum"),
    memory_usedutilization("Host.mem.usedutilization，内存使用率","%","instanceId","Average、Minimum、Maximum"),

    load_1m("Host.load1，过去1分钟的系统平均负载，Windows操作系统没有此指标","无","instanceId","Average、Minimum、Maximum"),
    load_5m("Host.load5， 过去5分钟的系统平均负载，Windows操作系统没有此指标","无","instanceId","Average、Minimum、Maximum"),
    load_15m("Host.load15，过去15分钟的系统平均负载，Windows操作系统没有此指标","无","instanceId","Average、Minimum、Maximum"),

    diskusage_used("Host.diskusage.used，磁盘的已用存储空间","byte","instanceId,device","Average、Minimum、Maximum"),
    diskusage_utilization("Host.disk.utilization，磁盘使用率","%","instanceId,device","Average、Minimum、Maximum"),
    diskusage_free("Host.diskusage.free，磁盘的剩余存储空间","byte/s","instanceId,device","Average、Minimum、Maximum"),
    diskusage_total("Host.diskussage.total，磁盘存储总量","byte","instanceId,device","Average、Minimum、Maximum"),
    disk_readbytes("Host.disk.readbytes，磁盘每秒读取的字节数","byte/s","instanceId,device","Average、Minimum、Maximum"),
    disk_writebytes("Host.disk.writebytes，磁盘每秒写入的字节数","byte/s","instanceId,device","Average、Minimum、Maximum"),
    disk_readiops("Host.disk.readiops，磁盘每秒的读请求数量","次/秒","instanceId,device","Average、Minimum、Maximum"),
    disk_writeiops("Host.disk.writeiops，磁盘每秒的写请求数量","次/秒","instanceId,device","Average、Minimum、Maximum"),
    fs_inodeutilization("Host.fs.inode，inode使用率","%","instanceId,device","Average、Minimum、Maximum"),

    networkin_rate("Host.netin.rate，网卡每秒接收的比特数，即网卡的上行带宽","bit/s","instanceId,device","Average、Minimum、Maximum"),
    networkout_rate("Host.netout.rate，网卡每秒发送的比特数，即网卡的下行带宽","bit/s","instanceId,device","Average、Minimum、Maximum"),

    networkin_packages("Host.netin.packages，网卡每秒接收的数据包数","个/秒","instanceId,device","Average、Minimum、Maximum"),
    networkout_packages("Host.netout.packages，网卡每秒发送的数据包数","个/秒","instanceId,device","Average、Minimum、Maximum"),
    networkin_errorpackages("Host.netin.errorpackage，设备驱动器检测到的接收错误包的数量","个/秒","instanceId,device","Average、Minimum、Maximum"),
    networkout_errorpackages("Host.netout.errorpackages，设备驱动器检测到的发送错误包的数量","个/秒","instanceId,device","Average、Minimum、Maximum"),
    net_tcpconnection("Host.tcpconnection，各种状态下的TCP连接数包括LISTEN、SYN_SENT、ESTABLISHED、SYN_RECV、FIN_WAIT1、CLOSE_WAIT、FIN_WAIT2、LAST_ACK、TIME_WAIT、CLOSING、CLOSED","个","instanceId,state","Average、Minimum、Maximum");


    private String describe;
    private String units;
    private String dimensionality;
    private String statistics;

    ECS(String describe, String units, String dimensionality, String statistics) {
        this.describe = describe;
        this.units = units;
        this.dimensionality = dimensionality;
        this.statistics = statistics;
    }
}
