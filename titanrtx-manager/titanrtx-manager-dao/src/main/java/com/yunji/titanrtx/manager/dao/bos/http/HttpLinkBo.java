package com.yunji.titanrtx.manager.dao.bos.http;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HttpLinkBo implements Serializable {

    private int id;

    private long requestTotal;

    private double duration;

    private long qps;

    private double averageDuration;

    private long Http200Code;

    private String Http200CodeRate;

    private long HttpOtherCode;

    private String HttpOtherCodeRate;

    private List<PairBo> pairBos;

}
