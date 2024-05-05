package com.yunji.titanrtx.plugin.monitor.tencent;

import com.yunji.titanrtx.plugin.monitor.AbstractExecute;
import com.yunji.titanrtx.plugin.monitor.Base;
import com.yunji.titanrtx.plugin.monitor.Execute;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import com.yunji.titanrtx.plugin.monitor.enums.VCM;

import java.util.List;

public class VCMBaseMonitor extends AbstractExecute<VCM> implements Base {

    private static final int PERIOD  = 60;

    VCMBaseMonitor(Execute<VCM> execute) {
        super(execute);
    }

    @Override
    public List<MonitorBo> cpuUtilization(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> internetInRate(String id, String start, String end) throws Exception {
        return execute(VCM.WanIntraffic,CVM_NAME_SPACE,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> intranetInRate(String id, String start, String end) throws Exception {
        return execute(VCM.lanIntraffic,CVM_NAME_SPACE,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> internetOutRate(String id, String start, String end) throws Exception {
        return execute(VCM.WanOuttraffic,CVM_NAME_SPACE,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> intranetOutRate(String id, String start, String end) throws Exception {
        return execute(VCM.lanOuttraffic,CVM_NAME_SPACE,PERIOD,id,start,end);
    }

    @Override
    public List<MonitorBo> internetOutRate_percent(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> diskReadBps(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> diskWriteBps(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> diskReadIOps(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> diskWriteIOps(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> vpc_publicIp_internetInRate(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> vpc_publicIp_internetOutRate(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorBo> vpc_publicIp_internetOutRate_percent(String id, String start, String end) throws Exception {
        throw new UnsupportedOperationException();
    }
}
