package com.yunji.titanrtx.manager.dao.bos;


import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import lombok.Data;

import java.util.List;

@Data
public class OverviewMonitorBo {

    private List<MonitorBo> netIn;

    private List<MonitorBo>  netOut;

}
