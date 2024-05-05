package com.yunji.titanrtx.manager.dao.bos;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.List;

@Data
public class PressureReportBo {

    protected int reportId;

    protected SummaryStatistics sum;

    protected BaseEntity sceneEntity;

    protected List<? extends BaseEntity> bulletEntity;

    protected List<String> linksUrl;

    protected Date startTime;

    protected Date endTime;

    // 增加了性能基线需要对基线进行误差控制
    protected int rtError; // rt允许误差
    protected int tpsError; // tps允许误差
}
