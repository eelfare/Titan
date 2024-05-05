package com.yunji.titanrtx.manager.dao.entity.dubbo;

import com.yunji.titanrtx.common.enums.Flow;
import com.yunji.titanrtx.common.enums.ParamTransmit;
import com.yunji.titanrtx.common.enums.Sequence;
import com.yunji.titanrtx.common.enums.Strategy;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DubboSceneEntity extends BaseEntity {

    private String name;

    private long concurrent;

    private long total;

    private long timeout;

    private long throughPut;

    private String idsWeight;

    private Strategy strategy;

    private Flow flow;

    private Sequence sequence;

    private int status;

    /**
     * 参数 fetch 模式.
     */
    private ParamTransmit paramTransmit;


    /**
     * 参数 fetch 模式. dubbo 的只支持 传参数 id list 的模式.
     *//*
    private ParamTransmit paramTransmit = ParamTransmit.IDS;*/

}
