package com.yunji.titanrtx.plugin.dubbo.support;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder(toBuilder = true)
@Data
public class RpcRequest {

    private String serviceName;

    private String method;
    /**
     * specificAddress 手工指定的地址,符合单个压测场景
     */
    private List<String> address;

    private String[] paramsType;

    private Object[] paramsValue;

    private String rpcContent;
    /**
     * 集群压测场景
     */
    private List<String> clusterAddress;

    private boolean cluster;

}
