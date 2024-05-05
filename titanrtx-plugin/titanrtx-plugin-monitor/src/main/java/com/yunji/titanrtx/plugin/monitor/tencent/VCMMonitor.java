package com.yunji.titanrtx.plugin.monitor.tencent;

import com.yunji.titanrtx.plugin.monitor.AbstractMonitor;
import com.yunji.titanrtx.plugin.monitor.Execute;
import com.yunji.titanrtx.plugin.monitor.enums.VCM;

public class VCMMonitor extends AbstractMonitor {


    public VCMMonitor(String regionId, String accessId, String secretKey) {
        Execute<VCM> execute  = new VCMExecute(regionId,accessId,secretKey);
        super.init(new VCMBaseMonitor(execute),new VCMSystemMonitor(execute));
    }



}
