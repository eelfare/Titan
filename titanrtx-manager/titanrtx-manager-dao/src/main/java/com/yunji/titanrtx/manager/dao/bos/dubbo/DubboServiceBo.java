package com.yunji.titanrtx.manager.dao.bos.dubbo;


import com.yunji.titanrtx.manager.dao.bos.http.PairBo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DubboServiceBo implements Serializable {

    private int id;

    private int requestTotal;

    private int duration;

    private int qps;

    private double averageDuration; //ms

    private int successCode;

    private String successCodeRate;

    private int failCode;

    private String failCodeRate;

    private List<PairBo> pairBos;

}
