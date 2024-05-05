package com.yunji.titanrtx.manager.web.controller;

import com.yunji.titanrtx.common.enums.Platform;
import com.yunji.titanrtx.common.u.CollectionU;
import com.yunji.titanrtx.manager.web.config.SystemConfig;
import com.yunji.titanrtx.plugin.monitor.Monitor;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MonitorService {

    @Resource
    private Monitor monitor;

    @Resource
    private SystemConfig systemConfig;


    public void doNetOutRateRequest(List<String> ins, String start, String end, List<MonitorBo> netOutBos) {
        for (String id : ins){
            try {
                List<MonitorBo> bos = doNetOutRateRequest(id,start,end);
                if (CollectionU.isNotEmpty(bos)) {
                    netOutBos.addAll(bos);
                }
            } catch (Exception ignored) {
            }
        }
    }


    public void doNetInRateRequest(List<String> ins, String start, String end, List<MonitorBo> netInBos) {
        for (String id : ins){
            try {
                List<MonitorBo> bos = doNetInRateRequest(id,start,end);
                if (CollectionU.isNotEmpty(bos)) {
                    netInBos.addAll(bos);
                }
            } catch (Exception ignored) {
            }
        }
    }



    public List<MonitorBo> cpuTotal(String id, String start, String end) throws Exception {
        return monitor.cpu_total(id,start,end);
    }

    public List<MonitorBo> memoryUsed(String id, String start, String end) throws Exception {
        return monitor.memory_usedutilization(id,start,end);
    }


    public List<MonitorBo> doNetInRateRequest(String id,String start, String end) throws Exception {
        String platform = systemConfig.getCloudPlatform();
        if (StringUtils.equalsIgnoreCase(platform, Platform.ALI.toString())){
            return advancedUnitMB(monitor.networkin_rate(id, start, end));
        }
        return monitor.intranetInRate(id, start, end);
    }


    public List<MonitorBo> doNetOutRateRequest(String id,String start, String end) throws Exception {
        String platform = systemConfig.getCloudPlatform();
        if (StringUtils.equalsIgnoreCase(platform, Platform.ALI.toString())){
            return advancedUnitMB(monitor.networkout_rate(id, start, end));
        }
        return monitor.intranetOutRate(id, start, end);
    }


    private List<MonitorBo> advancedUnitMB(List<MonitorBo> bos){
        bos.forEach(monitorBo -> {
            int value = (int)monitorBo.getValue();
            monitorBo.setValue(value >> 20);
        });
        return bos;
    }

}
