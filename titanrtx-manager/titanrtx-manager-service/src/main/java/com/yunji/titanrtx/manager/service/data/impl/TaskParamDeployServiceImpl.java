package com.yunji.titanrtx.manager.service.data.impl;

import com.yunji.titanrtx.manager.dao.entity.data.TaskParamDeployEntity;
import com.yunji.titanrtx.manager.dao.mapper.data.TaskParamDeployMapper;
import com.yunji.titanrtx.manager.service.data.TaskParamDeployService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 19:11
 * @Version 1.0
 */
@Service
public class TaskParamDeployServiceImpl implements TaskParamDeployService {

    @Resource
    TaskParamDeployMapper taskParamDeployMapper;

    @Override
    public int deleteById(Integer id) {
        return taskParamDeployMapper.deleteById(id);
    }

    @Override
    public int deleteAllByTaskId(Integer taskId) {
        return taskParamDeployMapper.deleteAllByTaskId(taskId);
    }

    @Override
    public Integer insert(TaskParamDeployEntity paramDeployEntity) {
        return taskParamDeployMapper.insert(paramDeployEntity);
    }

    @Override
    public int update(TaskParamDeployEntity paramDeployEntity) {
        return taskParamDeployMapper.update(paramDeployEntity);
    }

    @Override
    public List<TaskParamDeployEntity> selectByTaskId(Integer taskId) {
        return taskParamDeployMapper.selectByTaskId(taskId);
    }

    @Override
    public TaskParamDeployEntity findById(Integer id) {
        return taskParamDeployMapper.findById(id);
    }
}
