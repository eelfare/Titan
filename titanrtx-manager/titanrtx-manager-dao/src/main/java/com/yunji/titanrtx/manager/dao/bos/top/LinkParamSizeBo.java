package com.yunji.titanrtx.manager.dao.bos.top;

import lombok.Data;
import org.influxdb.annotation.Column;

@Data
public class LinkParamSizeBo {

    @Column(name = "param")
    private String param;

}
