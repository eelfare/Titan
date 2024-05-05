package com.yunji.titanrtx.manager.dao.bos.data;

import lombok.Data;

/** 参数对应的配置信息
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 15:25
 * @Version 1.0
 */
@Data
public class ParamSourceBo {
    private String paramName;  // 参数名
    private Integer type;       // 参数使用方式(0：可重用；1：不可重用；2：随机)
    private SQLDeployBo sqlDeployBo; // 关联的数据库信息
}
