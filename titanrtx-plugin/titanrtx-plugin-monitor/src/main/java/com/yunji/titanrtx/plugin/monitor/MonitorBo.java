package com.yunji.titanrtx.plugin.monitor;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class MonitorBo {

    /**
     * 阿里云响应格式不存在value字段，因此将average字段映射成value字段
     *
     * 注意区分单位  腾讯云涉及网络部分多数参数返回值为MB，阿里云为Byte
     *
     */
    @JSONField(name = "average")
    private double value;

    private long timestamp;

}
