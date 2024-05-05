package com.yunji.titanrtx.manager.dao.bos.system;

import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MachineMonitorBo {

    private List<MonitorBo>  cpu;

    private List<MonitorBo>  memory;

    private List<MonitorBo>  netIn;

    private List<MonitorBo>  netOut;


}
