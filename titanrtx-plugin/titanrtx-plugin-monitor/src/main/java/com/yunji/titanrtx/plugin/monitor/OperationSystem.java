package com.yunji.titanrtx.plugin.monitor;


import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OperationSystem {

    List<MonitorBo> cpu_idle(String id, String start, String end) throws Exception;
    List<MonitorBo> cpu_system(String id,String start,String end) throws Exception;
    List<MonitorBo> cpu_user(String id,String start,String end) throws Exception;
    List<MonitorBo> cpu_wait(String id,String start,String end) throws Exception;
    List<MonitorBo> cpu_other(String id,String start,String end) throws Exception;
    List<MonitorBo> cpu_total(String id,String start,String end) throws Exception;
    Map<String, List<MonitorBo>> cpu_total(Set<String> ids, String start, String end) throws Exception;

    List<MonitorBo> memory_totalspace(String id,String start,String end) throws Exception;
    List<MonitorBo> memory_usedspace(String id,String start,String end) throws Exception;
    List<MonitorBo> memory_actualusedspace(String id,String start,String end) throws Exception;
    List<MonitorBo> memory_freespace(String id,String start,String end) throws Exception;
    List<MonitorBo> memory_freeutilization(String id,String start,String end) throws Exception;
    List<MonitorBo> memory_usedutilization(String id,String start,String end) throws Exception;

    List<MonitorBo> load_1m(String id,String start,String end) throws Exception;
    List<MonitorBo> load_5m(String id,String start,String end) throws Exception;
    List<MonitorBo> load_15m(String id,String start,String end) throws Exception;

    List<MonitorBo> diskusage_used(String id,String start,String end) throws Exception;
    List<MonitorBo> diskusage_utilization(String id,String start,String end) throws Exception;
    List<MonitorBo> diskusage_free(String id,String start,String end) throws Exception;
    List<MonitorBo> diskusage_total(String id,String start,String end) throws Exception;
    List<MonitorBo> disk_readbytes(String id,String start,String end) throws Exception;
    List<MonitorBo> disk_writebytes(String id,String start,String end) throws Exception;
    List<MonitorBo> disk_readiops(String id,String start,String end) throws Exception;
    List<MonitorBo> disk_writeiops(String id,String start,String end) throws Exception;
    List<MonitorBo> fs_inodeutilization(String id,String start,String end) throws Exception;

    List<MonitorBo> networkin_rate(String id,String start,String end) throws Exception;
    List<MonitorBo> networkout_rate(String id,String start,String end) throws Exception;
    List<MonitorBo> networkin_packages(String id,String start,String end) throws Exception;
    List<MonitorBo> networkout_packages(String id,String start,String end) throws Exception;
    List<MonitorBo> networkin_errorpackages(String id,String start,String end) throws Exception;
    List<MonitorBo> networkout_errorpackages(String id,String start,String end) throws Exception;
    List<MonitorBo> net_tcpconnection(String id,String start,String end) throws Exception;

}
