package com.yunji.titanrtx.manager.service.auto.impl;

import com.yunji.titanrtx.manager.dao.entity.auto.BlackGroupEntity;
import com.yunji.titanrtx.manager.dao.mapper.auto.BlackGroupMapper;
import com.yunji.titanrtx.manager.service.auto.BlackGroupService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-27 22:45
 * @Version 1.0
 */
@Service
public class BlackGroupServiceImpl implements BlackGroupService {
    @Resource
    BlackGroupMapper mapper;

    @Override
    public List<BlackGroupEntity> selectAll() {
        return mapper.selectAll();
    }

    @Override
    public int deleteById(Integer id) {
        return mapper.deleteById(id);
    }


    @Override
    public int insert(BlackGroupEntity entity) {
        return mapper.insert(entity);
    }

    @Override
    public BlackGroupEntity findById(Integer id) {
        return mapper.findById(id);
    }

    @Override
    public Boolean findByName(String name) {
        return mapper.findByName(name);
    }

    @Override
    public int update(BlackGroupEntity entity) {
        return mapper.update(entity);
    }

    @Override
    public List<BlackGroupEntity> search(String key) {
        return mapper.search(key);
    }
}
