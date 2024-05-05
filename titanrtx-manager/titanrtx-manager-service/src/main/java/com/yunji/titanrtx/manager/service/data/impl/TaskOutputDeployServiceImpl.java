package com.yunji.titanrtx.manager.service.data.impl;

import com.yunji.titanrtx.manager.dao.entity.data.TaskOutputDeployEntity;
import com.yunji.titanrtx.manager.dao.mapper.data.TaskOutputDeployMapper;
import com.yunji.titanrtx.manager.service.data.TaskOutputDeployService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 19:11
 * @Version 1.0
 */
@Service
public class TaskOutputDeployServiceImpl implements TaskOutputDeployService {

    @Resource
    TaskOutputDeployMapper taskOutputDeployMapper;

    @Override
    public TaskOutputDeployEntity selectById(Integer id) {
        return taskOutputDeployMapper.selectById(id);
    }

    @Override
    public int deleteById(Integer id) {
        return taskOutputDeployMapper.deleteById(id);
    }

    @Override
    public int deleteByTaskIdAndExpr(Integer taskId, String expr) {
        return taskOutputDeployMapper.deleteByTaskIdAndExpr(taskId, expr);
    }

    @Override
    public int deleteAllByTaskId(Integer taskId) {
        return taskOutputDeployMapper.deleteAllByTaskId(taskId);
    }

    @Override
    public Integer insert(TaskOutputDeployEntity outputDeployEntity) {
        return taskOutputDeployMapper.insert(outputDeployEntity);
    }

    @Override
    public int update(TaskOutputDeployEntity outputDeployEntity) {
        return taskOutputDeployMapper.update(outputDeployEntity);
    }

    @Override
    public List<TaskOutputDeployEntity> selectByTaskId(Integer taskId) {
        return taskOutputDeployMapper.selectByTaskId(taskId);
    }
}
