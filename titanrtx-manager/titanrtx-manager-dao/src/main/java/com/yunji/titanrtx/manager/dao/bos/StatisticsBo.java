package com.yunji.titanrtx.manager.dao.bos;

import com.yunji.titanrtx.manager.dao.bos.http.PairBo;
import lombok.Data;

import java.util.List;
@Data
public class StatisticsBo {

    private int id;

    private long requestTotal;

    private double duration;

    private long qps;

    private double averageDuration;

    private long  requestSuccessCode;

    private String requestSuccessCodeRate;

    private long requestFailCode;

    private String requestFailCodeRate;

    // 新增性能基线参考
    private double avgRt; // 平均机器负载
    private long avgDisposeCount; // 平均机器处理总量

    private List<PairBo> pairBos;

}
