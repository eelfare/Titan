package com.yunji.titanrtx.manager.dao.bos.top;

import lombok.Data;
import org.influxdb.annotation.Column;

import java.io.Serializable;

@Data
public class TopLinkBo implements Serializable {

    @Column(name = "domain",tag = true)
    private String domain;

    @Column(name = "path",tag = true)
    private String path;

    @Column(name = "successTimes")
    private long successTimes;

    @Column(name = "requestTimes")
    private long requestTimes;

    @Column(name = "elapsed")
    private double elapsed;
}
