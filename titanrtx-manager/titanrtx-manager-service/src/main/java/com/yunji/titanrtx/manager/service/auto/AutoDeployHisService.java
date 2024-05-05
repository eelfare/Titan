package com.yunji.titanrtx.manager.service.auto;

import com.yunji.titanrtx.manager.dao.entity.auto.AutoDeployHisEntity;

import java.util.List;

/**
 * 自动化任务配置历史
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-27 22:44
 * @Version 1.0
 */
public interface AutoDeployHisService {
    List<AutoDeployHisEntity> selectAll();

    int deleteById(Integer id);

    int insert(AutoDeployHisEntity entity);

    AutoDeployHisEntity findById(Integer id);

    int update(AutoDeployHisEntity entity);
}
