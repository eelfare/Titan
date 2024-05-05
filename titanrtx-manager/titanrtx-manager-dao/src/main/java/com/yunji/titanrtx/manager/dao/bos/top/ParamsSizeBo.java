package com.yunji.titanrtx.manager.dao.bos.top;

import lombok.Data;
import org.influxdb.annotation.Column;

@Data
public class ParamsSizeBo {

    @Column(name = "count")
    private int count;

}
