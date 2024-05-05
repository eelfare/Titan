package com.yunji.titanrtx.manager.dao.bos.dubbo;

import lombok.Data;

/**
 * @program: titanrtx
 * @description: 远程dubbo执行BO
 * @author: Jingf.Pang
 * @create: 2020-11-09 10:02
 **/
@Data
public class RemoteDubboRequestBO {
    /**
     * 应用名
     */
    private String app;
    /**
     * 服务名
     */
    private String service;
    /**
     * 方法名
     */
    private String method;
    /**
     * 参数类型（有序）
     */
    private String paramsTypes;
    /**
     * 单次调用参数
     */
    private String params;
    /**
     * rpc参数
     */
    private String rpcParams;
}
