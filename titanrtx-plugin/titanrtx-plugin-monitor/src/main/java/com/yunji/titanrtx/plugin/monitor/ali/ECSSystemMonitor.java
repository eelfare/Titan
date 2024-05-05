package com.yunji.titanrtx.plugin.monitor.ali;

import com.yunji.titanrtx.plugin.monitor.AbstractExecute;
import com.yunji.titanrtx.plugin.monitor.Execute;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import com.yunji.titanrtx.plugin.monitor.OperationSystem;
import com.yunji.titanrtx.plugin.monitor.enums.ECS;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ECSSystemMonitor extends AbstractExecute<ECS> implements OperationSystem {

    private static final int PERIOD  = 60;

    ECSSystemMonitor(Execute<ECS> execute) {
        super(execute);
    }

    @Override
    public List<MonitorBo> cpu_idle(String id, String start, String end) throws Exception {
        return execute(ECS.cpu_idle,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_system(String id,String start,String end) throws Exception {
        return execute(ECS.cpu_system,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_user(String id,String start,String end) throws Exception {
        return execute(ECS.cpu_user,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_wait(String id,String start,String end) throws Exception {
        return execute(ECS.cpu_wait,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_other(String id,String start,String end) throws Exception {
        return execute(ECS.cpu_other,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> cpu_total(String id,String start,String end) throws Exception {
        return execute(ECS.cpu_total,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public Map<String, List<MonitorBo>> cpu_total(Set<String> ids, String start, String end) throws Exception {
        return execute(ECS.cpu_total, ECS_PROJECT_NAME, PERIOD, ids, start, end);
    }

    @Override
    public List<MonitorBo> memory_totalspace(String id,String start,String end) throws Exception {
        return execute(ECS.memory_totalspace,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> memory_usedspace(String id,String start,String end) throws Exception {
        return execute(ECS.memory_usedspace,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> memory_actualusedspace(String id,String start,String end) throws Exception {
        return execute(ECS.memory_actualusedspace,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> memory_freespace(String id,String start,String end) throws Exception {
        return execute(ECS.memory_freespace,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> memory_freeutilization(String id,String start,String end) throws Exception {
        return execute(ECS.memory_freeutilization,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> memory_usedutilization(String id,String start,String end) throws Exception {
        return execute(ECS.memory_usedutilization,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> load_1m(String id,String start,String end) throws Exception {
        return execute(ECS.load_1m,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> load_5m(String id,String start,String end) throws Exception {
        return execute(ECS.load_5m,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> load_15m(String id,String start,String end) throws Exception {
        return execute(ECS.load_15m,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> diskusage_used(String id,String start,String end) throws Exception {
        return execute(ECS.diskusage_used,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> diskusage_utilization(String id,String start,String end) throws Exception {
        return execute(ECS.diskusage_utilization,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> diskusage_free(String id,String start,String end) throws Exception {
        return execute(ECS.diskusage_free,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> diskusage_total(String id,String start,String end) throws Exception {
        return execute(ECS.diskusage_total,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> disk_readbytes(String id,String start,String end) throws Exception {
        return execute(ECS.disk_readbytes,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> disk_writebytes(String id,String start,String end) throws Exception {
        return execute(ECS.disk_writebytes,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> disk_readiops(String id,String start,String end) throws Exception {
        return execute(ECS.disk_readiops,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> disk_writeiops(String id,String start,String end) throws Exception {
        return execute(ECS.disk_writeiops,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> fs_inodeutilization(String id,String start,String end) throws Exception {
        return execute(ECS.fs_inodeutilization,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> networkin_rate(String id,String start,String end) throws Exception {
        return execute(ECS.networkin_rate,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> networkout_rate(String id,String start,String end) throws Exception {
        return execute(ECS.networkout_rate,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> networkin_packages(String id,String start,String end) throws Exception {
        return execute(ECS.networkin_packages,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> networkout_packages(String id,String start,String end) throws Exception {
        return execute(ECS.networkin_errorpackages,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> networkin_errorpackages(String id,String start,String end) throws Exception {
        return execute(ECS.networkin_errorpackages,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> networkout_errorpackages(String id,String start,String end) throws Exception {
        return execute(ECS.networkout_errorpackages,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> net_tcpconnection(String id,String start,String end) throws Exception {
        return execute(ECS.net_tcpconnection,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

}
