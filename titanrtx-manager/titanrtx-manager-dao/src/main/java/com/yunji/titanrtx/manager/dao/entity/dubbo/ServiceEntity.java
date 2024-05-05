package com.yunji.titanrtx.manager.dao.entity.dubbo;

import com.yunji.titanrtx.common.enums.ParamMode;
import com.yunji.titanrtx.common.enums.ParamTransmit;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceEntity extends BaseEntity {

    private String name;

    private String applicationName;

    private String serviceName;

    private String methodName;

    private String paramsType;

    private String rpcContent;

    private String address;

    /**
     * Dubbo 集群所有节点地址 "," 分隔
     */
    private String clusterAddress;

    @Transient
    private long weight = 100;

    @Transient
    private long paramNum = 0;

    /**
     * 随机 or 顺序 = 默认随机
     */
    private ParamMode paramMode = ParamMode.RANDOM;

    /**
     * 链路对应的参数的顺序性质
     */
    private int paramStatus;


}
