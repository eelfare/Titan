package com.yunji.titanrtx.manager.dao.bos;

import lombok.Data;

/**
 * 性能基线指标
 *
 * @Author: 景风（彭秋雁）
 * @Date: 13/4/2020 10:53 上午
 * @Version 1.0
 */
@Data
public class BaseLineBo {
    private double avgRt = 0; // 平均机器负载
    private long avgDisposeCount = 0L; // 平均机器处理总量
}
