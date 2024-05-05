package com.yunji.titanrtx.common.domain.task;

import com.yunji.titanrtx.common.enums.ParamMode;
import com.yunji.titanrtx.common.enums.ParamTransmit;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Bullet implements Serializable {
    /**
     * 链路id
     */
    protected Integer id;
    /**
     * 压测权重
     */
    protected long weight;
    /**
     * 存储链路实际参数
     */
    protected List<String> params;
    /**
     * 存储链路参数的id
     */
    protected List<Integer> paramIds;

    /**
     * 获取参数的方式
     */
    protected ParamMode paramMode;
    /**
     * 参数顺序消费模式下，当前读取参数的位置;
     */
    protected int current = 0;
    /**
     * ParamTransmit 模式为 orders 时,使用.使用参数范围，例如 1 - 200, 1-25000.
     */
    protected ParamRange paramRange;

    protected ParamTransmit transmit;

}
