package com.yunji.titanrtx.manager.service.report.dto;

import lombok.Data;

import java.util.List;

/**
 * 链路监控 monitoring 时封装的最终告警报告
 */
@Data
public class AlertReportDTO {

    private String sceneName;

    private long startTime;

    private long endTime;

    private List<LinkStatisticsDTO> detailBoList;


}
