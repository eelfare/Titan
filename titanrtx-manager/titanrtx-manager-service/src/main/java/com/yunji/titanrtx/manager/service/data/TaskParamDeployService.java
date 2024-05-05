package com.yunji.titanrtx.manager.service.data;

import com.yunji.titanrtx.manager.dao.entity.data.TaskParamDeployEntity;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:53
 * @Version 1.0
 */
public interface TaskParamDeployService {

    int deleteById(Integer id);

    int deleteAllByTaskId(Integer taskId);

    Integer insert(TaskParamDeployEntity paramDeployEntity);

    int update(TaskParamDeployEntity paramDeployEntity);

    List<TaskParamDeployEntity> selectByTaskId(Integer taskId);

    TaskParamDeployEntity findById(Integer id);
}
