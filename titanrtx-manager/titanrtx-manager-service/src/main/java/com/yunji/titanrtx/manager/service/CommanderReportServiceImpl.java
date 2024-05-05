package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.service.CommanderReportService;
import com.yunji.titanrtx.manager.boot.ReportHandler;

import javax.annotation.Resource;


public class CommanderReportServiceImpl implements CommanderReportService {


    @Resource
    private ReportHandler reportHandler;


    @Override
    public void report(Statistics statistics) {
        reportHandler.report(statistics);
    }

}
