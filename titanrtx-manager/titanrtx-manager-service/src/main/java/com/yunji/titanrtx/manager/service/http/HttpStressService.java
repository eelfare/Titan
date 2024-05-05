package com.yunji.titanrtx.manager.service.http;

import com.yunji.titanrtx.common.message.RespMsg;

/**
 * HttpStressTestService HTTP压测接口.
 *
 * @author leihz
 * @since 2020-05-24 11:44 上午
 */
public interface HttpStressService {
    /**
     * 开始压测
     *
     * @param id 根据输入的场景开启压测
     */
    RespMsg startPressure(int id);


    Object reportSceneResult(int id, boolean onlyShowError,boolean deleteBadLinkParam);
}
