package com.yunji.titanrtx.manager.service.data;

import com.yunji.titanrtx.manager.dao.entity.data.TaskEntity;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:53
 * @Version 1.0
 */
public interface TaskService {
    List<TaskEntity> selectAll();

    TaskEntity findById(int id);

    TaskEntity findByUrl(String url);

    int deleteById(int id);

    Integer insert(TaskEntity task);

    int update(TaskEntity task);

    List<TaskEntity> searchTasks(String key);
}
