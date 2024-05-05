package com.yunji.titanrtx.common.domain.task;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class DubboService extends Bullet {

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

}
