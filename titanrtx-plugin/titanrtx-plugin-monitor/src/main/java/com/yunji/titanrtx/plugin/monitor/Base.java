package com.yunji.titanrtx.plugin.monitor;

import java.util.List;

public interface Base {

    List<MonitorBo> cpuUtilization(String id,String start,String end) throws Exception;

    List<MonitorBo> internetInRate(String id, String start, String end) throws Exception;

    List<MonitorBo> intranetInRate(String id,String start,String end) throws Exception;

    List<MonitorBo> internetOutRate(String id,String start,String end) throws Exception;

    List<MonitorBo> intranetOutRate(String id,String start,String end) throws Exception;

    List<MonitorBo> internetOutRate_percent(String id,String start,String end) throws Exception;

    List<MonitorBo> diskReadBps(String id,String start,String end) throws Exception;

    List<MonitorBo> diskWriteBps(String id,String start,String end) throws Exception;

    List<MonitorBo> diskReadIOps(String id,String start,String end) throws Exception;

    List<MonitorBo> diskWriteIOps(String id,String start,String end) throws Exception;

    List<MonitorBo> vpc_publicIp_internetInRate(String id,String start,String end) throws Exception;

    List<MonitorBo> vpc_publicIp_internetOutRate(String id,String start,String end) throws Exception;

    List<MonitorBo> vpc_publicIp_internetOutRate_percent(String id,String start,String end) throws Exception;

}
