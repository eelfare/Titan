package com.yunji.titanrtx.manager.service.dubbo;

import com.yunji.titanrtx.common.message.RespMsg;

/**
 * DubboStressService HTTP压测接口.
 *
 * @author leihz
 * @since 2020-07-02 04:08 下午
 */
public interface DubboStressService {
    /**
     * 开始压测
     *
     * @param id 根据输入的场景开启压测
     */
    RespMsg startPressure(int id);


    Object reportSceneResult(int id, boolean onlyShowError, boolean deleteBadLinkParam);
}
