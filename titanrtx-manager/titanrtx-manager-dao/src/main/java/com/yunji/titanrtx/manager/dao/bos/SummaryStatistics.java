package com.yunji.titanrtx.manager.dao.bos;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SummaryStatistics implements Serializable {

    protected long requestTotal;

    protected double totalDuration;

    protected long  executionDuration;

    protected long  qps;

    protected double averageDuration;

    private long requestSuccessCode;

    private String requestSuccessCodeRate;

    private long requestFailCode;

    private String requestFailCodeRate;

    private long businessSuccessCode;

    private String businessSuccessCodeRate;

    private long businessFailCode;

    private String businessFailCodeRate;

    private List<StatisticsBo> bos;

    private List<BaseLineBo> baseLineBos; // 参考基线
}
