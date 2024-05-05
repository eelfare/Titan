package com.yunji.titanrtx.manager.dao.mapper.data;

import com.yunji.titanrtx.manager.dao.entity.data.TaskOutputDeployEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:40
 * @Version 1.0
 */
public interface TaskOutputDeployMapper {
    TaskOutputDeployEntity selectById(Integer id);

    int deleteById(Integer id);

    int deleteByTaskIdAndExpr(@Param("taskId") Integer taskId, @Param("expr") String expr);

    int deleteAllByTaskId(Integer taskId);

    Integer insert(TaskOutputDeployEntity outputDeployEntity);

    int update(TaskOutputDeployEntity outputDeployEntity);

    List<TaskOutputDeployEntity> selectByTaskId(Integer taskId);
}
