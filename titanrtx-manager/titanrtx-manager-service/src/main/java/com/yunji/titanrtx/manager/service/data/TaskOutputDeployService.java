package com.yunji.titanrtx.manager.service.data;

import com.yunji.titanrtx.manager.dao.entity.data.TaskOutputDeployEntity;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:53
 * @Version 1.0
 */
public interface TaskOutputDeployService {
    TaskOutputDeployEntity selectById(Integer id);

    int deleteById(Integer id);

    int deleteByTaskIdAndExpr(Integer taskId, String expr);

    int deleteAllByTaskId(Integer taskId);

    Integer insert(TaskOutputDeployEntity outputDeployEntity);

    int update(TaskOutputDeployEntity outputDeployEntity);

    List<TaskOutputDeployEntity> selectByTaskId(Integer taskId);
}
