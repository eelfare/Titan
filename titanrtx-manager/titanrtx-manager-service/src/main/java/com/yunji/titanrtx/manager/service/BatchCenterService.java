package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.manager.dao.bos.data.TaskSourceBo;
import com.yunji.titanrtx.manager.dao.entity.data.TaskParamSourceEntity;

/**
 * 流量构造批次管理中心
 */
public interface BatchCenterService {

    /**
     * 重置批次
     * @param batchId
     * @return
     */
    boolean reset(Integer batchId);

    /**
     * 批次执行
     * @param batchId
     * @return
     */
    void start(Integer batchId) throws Exception;

    /**
     * 获取任务配置
     * @param paramSourceEntity
     * @return
     */
    TaskSourceBo getTaskSource(TaskParamSourceEntity paramSourceEntity);
}
