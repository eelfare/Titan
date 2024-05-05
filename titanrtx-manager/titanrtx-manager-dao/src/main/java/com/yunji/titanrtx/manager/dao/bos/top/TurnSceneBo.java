package com.yunji.titanrtx.manager.dao.bos.top;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TurnSceneBo implements Serializable {

    private String name;
    // 链路参数个数
    private int linkParamsNum;
    // 链路协议
    private String linkParamsTreaty;
    // 各个路径的所含比例
    private List<Long> scale;
    private List<String> url;

}
