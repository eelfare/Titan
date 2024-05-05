package com.yunji.titanrtx.manager.dao.mapper.auto;

import com.yunji.titanrtx.manager.dao.entity.auto.AutoDeployHisEntity;

import java.util.List;

/**
 * 压测区历史记录
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-27 22:49
 * @Version 1.0
 */
public interface AutoDeployHisMapper {
    List<AutoDeployHisEntity> selectAll();

    int deleteById(Integer id);

    int insert(AutoDeployHisEntity entity);
    int update(AutoDeployHisEntity entity);

    AutoDeployHisEntity findById(Integer id);
}
