package com.yunji.titanrtx.plugin.monitor.ali;

import com.yunji.titanrtx.plugin.monitor.AbstractMonitor;
import com.yunji.titanrtx.plugin.monitor.Execute;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import com.yunji.titanrtx.plugin.monitor.enums.ECS;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ECSMonitor extends AbstractMonitor {


    public ECSMonitor(String regionId, String accessId, String secretKey) {
        Execute<ECS>  execute  = new ECSExecute(regionId,accessId,secretKey);
        super.init(new ECSBaseMonitor(execute),new ECSSystemMonitor(execute));
    }

}
