package com.yunji.titanrtx.manager.service.data.impl;

import com.yunji.titanrtx.manager.dao.entity.data.TaskEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskOutputDeployEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskParamDeployEntity;
import com.yunji.titanrtx.manager.dao.mapper.data.TaskMapper;
import com.yunji.titanrtx.manager.service.data.TaskOutputDeployService;
import com.yunji.titanrtx.manager.service.data.TaskParamDeployService;
import com.yunji.titanrtx.manager.service.data.TaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:55
 * @Version 1.0
 */
@Service(value = "Task")
public class TaskServiceImpl implements TaskService {
    @Resource
    TaskMapper taskMapper;

    @Resource
    TaskParamDeployService taskParamDeployService;
    @Resource
    TaskOutputDeployService taskOutputDeployService;

    @Override
    public List<TaskEntity> selectAll() {
        return taskMapper.selectAll();
    }

    @Override
    public TaskEntity findById(int id) {
        TaskEntity task = taskMapper.findById(id);
        if (task == null) return null;
        List<TaskParamDeployEntity> params = taskParamDeployService.selectByTaskId(task.getId());
        List<TaskOutputDeployEntity> outputs = taskOutputDeployService.selectByTaskId(task.getId());

        task.setListParamDeploy(params);
        task.setListOutputDeploy(outputs);
        return task;
    }

    @Override
    public TaskEntity findByUrl(String url) {
        return taskMapper.findByUrl(url);
    }

    @Override
    public int deleteById(int id) {
        return taskMapper.deleteById(id);
    }

    @Override
    public Integer insert(TaskEntity task) {
        return taskMapper.insert(task);
    }

    @Override
    public int update(TaskEntity task) {
        return taskMapper.update(task);
    }

    @Override
    public List<TaskEntity> searchTasks(String key) {
        return taskMapper.searchTasks(key);
    }
}
