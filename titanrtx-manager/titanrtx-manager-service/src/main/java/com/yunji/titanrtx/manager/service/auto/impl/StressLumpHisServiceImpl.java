package com.yunji.titanrtx.manager.service.auto.impl;

import com.yunji.titanrtx.manager.dao.entity.auto.AutoDeployHisEntity;
import com.yunji.titanrtx.manager.dao.mapper.auto.AutoDeployHisMapper;
import com.yunji.titanrtx.manager.service.auto.AutoDeployHisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-27 22:45
 * @Version 1.0
 */
@Service
public class StressLumpHisServiceImpl implements AutoDeployHisService {
    @Resource
    AutoDeployHisMapper mapper;

    @Override
    public List<AutoDeployHisEntity> selectAll() {
        return mapper.selectAll();
    }

    @Override
    public int deleteById(Integer id) {
        return mapper.deleteById(id);
    }

    @Override
    public int insert(AutoDeployHisEntity entity) {
        return mapper.insert(entity);
    }

    @Override
    public int update(AutoDeployHisEntity entity) {
        return mapper.update(entity);
    }

    @Override
    public AutoDeployHisEntity findById(Integer id) {
        return mapper.findById(id);
    }
}
