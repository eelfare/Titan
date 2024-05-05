package com.yunji.titanrtx.manager.service.report.dto;

import com.yunji.titanrtx.manager.dao.bos.PressureReportBo;
import com.yunji.titanrtx.manager.dao.bos.StatisticsBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PressureReportDTO extends PressureReportBo {
    /**
     * 专用于性能基线IDC环境不显示,其他显示.
     */
    private List<StatisticsBo> bos;
}
