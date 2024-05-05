package com.yunji.titanrtx.plugin.monitor.ali;

import com.yunji.titanrtx.plugin.monitor.AbstractExecute;
import com.yunji.titanrtx.plugin.monitor.Base;
import com.yunji.titanrtx.plugin.monitor.Execute;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import com.yunji.titanrtx.plugin.monitor.enums.ECS;

import java.util.List;

public class ECSBaseMonitor extends AbstractExecute<ECS> implements Base {

    private static final int PERIOD  = 60;

    ECSBaseMonitor(Execute<ECS> execute) {
        super(execute);
    }

    @Override
    public List<MonitorBo> cpuUtilization(String id, String start, String end) throws Exception {
        return execute(ECS.cpuUtilization,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> internetInRate(String id,String start,String end) throws Exception {
        return execute(ECS.internetInRate,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> intranetInRate(String id,String start,String end) throws Exception {
        return execute(ECS.intranetInRate,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> internetOutRate(String id,String start,String end) throws Exception {
        return execute(ECS.internetOutRate,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> intranetOutRate(String id,String start,String end) throws Exception {
        return execute(ECS.intranetOutRate,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> internetOutRate_percent(String id,String start,String end) throws Exception {
        return execute(ECS.internetOutRate_percent,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> diskReadBps(String id,String start,String end) throws Exception {
        return execute(ECS.diskReadBps,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> diskWriteBps(String id,String start,String end) throws Exception {
        return execute(ECS.diskWriteBps,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> diskReadIOps(String id,String start,String end) throws Exception {
        return execute(ECS.diskReadIOps,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> diskWriteIOps(String id,String start,String end) throws Exception {
        return execute(ECS.diskWriteIOps,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> vpc_publicIp_internetInRate(String id,String start,String end) throws Exception {
        return execute(ECS.vpc_publicIp_internetInRate,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> vpc_publicIp_internetOutRate(String id,String start,String end) throws Exception {
        return execute(ECS.vpc_publicIp_internetOutRate,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> vpc_publicIp_internetOutRate_percent(String id,String start,String end) throws Exception {
        return execute(ECS.vpc_publicIp_internetOutRate_percent,ECS_PROJECT_NAME,PERIOD,id,start,end);
    }

}
