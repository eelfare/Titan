package com.yunji.titanrtx.plugin.monitor.tencent;

import com.yunji.titanrtx.plugin.monitor.AbstractExecute;
import com.yunji.titanrtx.plugin.monitor.Execute;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import com.yunji.titanrtx.plugin.monitor.OperationSystem;
import com.yunji.titanrtx.plugin.monitor.enums.VCM;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class VCMSystemMonitor extends AbstractExecute<VCM> implements OperationSystem {

    private static final String NAME_SPACE="QCE/CVM";

    private static final int PERIOD  = 60;

    VCMSystemMonitor(Execute<VCM> execute) {
        super(execute);
    }

    @Override
    public List<MonitorBo> cpu_idle(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> cpu_system(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> cpu_user(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> cpu_wait(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> cpu_other(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> cpu_total(String id, String start, String end) throws Exception {
        return execute(VCM.CPUUsage,NAME_SPACE,PERIOD,id,start,end);
    }

    @Override
    public Map<String, List<MonitorBo>> cpu_total(Set<String> ids, String start, String end) throws Exception {
        return execute(VCM.CPUUsage,NAME_SPACE,PERIOD,ids,start,end);
    }

    @Override
    public List<MonitorBo> memory_totalspace(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> memory_usedspace(String id, String start, String end) throws Exception {
        return execute(VCM.MemUsed,NAME_SPACE,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> memory_actualusedspace(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> memory_freespace(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> memory_freeutilization(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> memory_usedutilization(String id, String start, String end) throws Exception {
        return execute(VCM.MemUsage,NAME_SPACE,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> load_1m(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> load_5m(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> load_15m(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> diskusage_used(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> diskusage_utilization(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> diskusage_free(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> diskusage_total(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> disk_readbytes(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> disk_writebytes(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> disk_readiops(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> disk_writeiops(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> fs_inodeutilization(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> networkin_rate(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> networkout_rate(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> networkin_packages(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> networkout_packages(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> networkin_errorpackages(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> networkout_errorpackages(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> net_tcpconnection(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }
}
