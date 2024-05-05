package com.yunji.titanrtx.plugin.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMonitor implements Monitor {

    private Base baseMonitor;

    private OperationSystem systemMonitor;


    public void init(Base baseMonitor, OperationSystem systemMonitor) {
        this.baseMonitor = baseMonitor;
        this.systemMonitor = systemMonitor;
    }


    @Override
    public List<MonitorBo> cpuUtilization(String id, String start, String end) throws Exception {
        return baseMonitor.cpuUtilization(id,start,end);
    }

    @Override
    public List<MonitorBo> internetInRate(String id,String start,String end) throws Exception {
        return baseMonitor.internetInRate(id,start,end);
    }

    @Override
    public List<MonitorBo> intranetInRate(String id,String start,String end) throws Exception {
        return baseMonitor.intranetInRate(id,start,end);
    }

    @Override
    public List<MonitorBo> internetOutRate(String id,String start,String end) throws Exception {
        return baseMonitor.internetOutRate(id,start,end);
    }

    @Override
    public List<MonitorBo> intranetOutRate(String id,String start,String end) throws Exception {
        return baseMonitor.intranetOutRate(id,start,end);
    }

    @Override
    public List<MonitorBo> internetOutRate_percent(String id,String start,String end) throws Exception {
        return baseMonitor.internetOutRate_percent(id,start,end);
    }

    @Override
    public List<MonitorBo> diskReadBps(String id,String start,String end) throws Exception {
        return baseMonitor.diskReadBps(id,start,end);
    }

    @Override
    public List<MonitorBo> diskWriteBps(String id,String start,String end) throws Exception {
        return baseMonitor.diskWriteBps(id,start,end);
    }

    @Override
    public List<MonitorBo> diskReadIOps(String id, String start, String end) throws Exception {
        return baseMonitor.diskReadIOps(id,start,end);
    }

    @Override
    public List<MonitorBo> diskWriteIOps(String id,String start,String end) throws Exception {
        return baseMonitor.diskWriteIOps(id,start,end);
    }

    @Override
    public List<MonitorBo> vpc_publicIp_internetInRate(String id,String start,String end) throws Exception {
        return baseMonitor.vpc_publicIp_internetInRate(id,start,end);
    }

    @Override
    public List<MonitorBo> vpc_publicIp_internetOutRate(String id,String start,String end) throws Exception {
        return baseMonitor.vpc_publicIp_internetOutRate(id,start,end);
    }

    @Override
    public List<MonitorBo> vpc_publicIp_internetOutRate_percent(String id,String start,String end) throws Exception {
        return baseMonitor.vpc_publicIp_internetOutRate_percent(id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_idle(String id,String start,String end) throws Exception {
        return systemMonitor.cpu_idle(id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_system(String id,String start,String end) throws Exception {
        return systemMonitor.cpu_system(id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_user(String id,String start,String end) throws Exception {
        return systemMonitor.cpu_user(id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_wait(String id,String start,String end) throws Exception {
        return systemMonitor.cpu_wait(id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_other(String id,String start,String end) throws Exception {
        return systemMonitor.cpu_other(id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_total(String id,String start,String end) throws Exception {
        return systemMonitor.cpu_total(id,start,end);
    }

    @Override
    public Map<String, List<MonitorBo>> cpu_total(Set<String> ids, String start, String end) throws Exception {
        return systemMonitor.cpu_total(ids,start,end);
    }

    @Override
    public List<MonitorBo> memory_totalspace(String id,String start,String end) throws Exception {
        return systemMonitor.memory_totalspace(id,start,end);
    }

    @Override
    public List<MonitorBo> memory_usedspace(String id,String start,String end) throws Exception {
        return systemMonitor.memory_usedspace(id,start,end);
    }

    @Override
    public List<MonitorBo> memory_actualusedspace(String id,String start,String end) throws Exception {
        return systemMonitor.memory_actualusedspace(id,start,end);
    }

    @Override
    public List<MonitorBo> memory_freespace(String id,String start,String end) throws Exception {
        return systemMonitor.memory_freespace(id,start,end);
    }

    @Override
    public List<MonitorBo> memory_freeutilization(String id,String start,String end) throws Exception {
        return systemMonitor.memory_freeutilization(id,start,end);
    }

    @Override
    public List<MonitorBo> memory_usedutilization(String id,String start,String end) throws Exception {
        return systemMonitor.memory_usedutilization(id,start,end);
    }

    @Override
    public List<MonitorBo> load_1m(String id,String start,String end) throws Exception {
        return systemMonitor.load_1m(id,start,end);
    }

    @Override
    public List<MonitorBo> load_5m(String id,String start,String end) throws Exception {
        return systemMonitor.load_5m(id,start,end);
    }

    @Override
    public List<MonitorBo> load_15m(String id,String start,String end) throws Exception {
        return systemMonitor.load_15m(id,start,end);
    }

    @Override
    public List<MonitorBo> diskusage_used(String id,String start,String end) throws Exception {
        return systemMonitor.diskusage_used(id,start,end);
    }

    @Override
    public List<MonitorBo> diskusage_utilization(String id,String start,String end) throws Exception {
        return systemMonitor.diskusage_utilization(id,start,end);
    }

    @Override
    public List<MonitorBo> diskusage_free(String id,String start,String end) throws Exception {
        return systemMonitor.diskusage_free(id,start,end);
    }

    @Override
    public List<MonitorBo> diskusage_total(String id,String start,String end) throws Exception {
        return systemMonitor.diskusage_total(id,start,end);
    }

    @Override
    public List<MonitorBo> disk_readbytes(String id,String start,String end) throws Exception {
        return systemMonitor.disk_readbytes(id,start,end);
    }

    @Override
    public List<MonitorBo> disk_writebytes(String id,String start,String end) throws Exception {
        return systemMonitor.disk_writebytes(id,start,end);
    }

    @Override
    public List<MonitorBo> disk_readiops(String id,String start,String end) throws Exception {
        return systemMonitor.disk_readiops(id,start,end);
    }

    @Override
    public List<MonitorBo> disk_writeiops(String id,String start,String end) throws Exception {
        return systemMonitor.disk_writeiops(id,start,end);
    }

    @Override
    public List<MonitorBo> fs_inodeutilization(String id,String start,String end) throws Exception {
        return systemMonitor.fs_inodeutilization(id,start,end);
    }

    @Override
    public List<MonitorBo> networkin_rate(String id,String start,String end) throws Exception {
        return systemMonitor.networkin_rate(id,start,end);
    }

    @Override
    public List<MonitorBo> networkout_rate(String id,String start,String end) throws Exception {
        return systemMonitor.networkout_rate(id,start,end);
    }

    @Override
    public List<MonitorBo> networkin_packages(String id,String start,String end) throws Exception {
        return systemMonitor.networkin_packages(id,start,end);
    }

    @Override
    public List<MonitorBo> networkout_packages(String id,String start,String end) throws Exception {
        return systemMonitor.networkout_packages(id,start,end);
    }

    @Override
    public List<MonitorBo> networkin_errorpackages(String id,String start,String end) throws Exception {
        return systemMonitor.networkin_errorpackages(id,start,end);
    }

    @Override
    public List<MonitorBo> networkout_errorpackages(String id,String start,String end) throws Exception {
        return systemMonitor.networkout_errorpackages(id,start,end);
    }

    @Override
    public List<MonitorBo> net_tcpconnection(String id,String start,String end) throws Exception {
        return systemMonitor.net_tcpconnection(id,start,end);
    }
}
