package com.yunji.titanrtx.manager.dao.entity.http;

import com.yunji.titanrtx.common.enums.*;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HttpSceneEntity extends BaseEntity {

    private String name;

    private long concurrent;

    private long total;

    private long timeout;

    private Strategy strategy;

    private long throughPut;

    private String idsScale;

    private String idsWeight;

    private String idsQps;

    private Flow flow;

    private Sequence sequence;

    private Allot allot;

    private int status;

    private String webhook; // 场景压测报告通知群
    /**
     * 参数传递方式.
     */
    private ParamTransmit paramTransmit;
    /**
     * 监控链路告警阈值,失败比率
     */
    private String alertThreshold;
}
