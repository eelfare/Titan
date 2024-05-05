package com.yunji.titanrtx.manager.service.auto.impl;

import com.yunji.titanrtx.manager.dao.entity.auto.FilterEntity;
import com.yunji.titanrtx.manager.dao.mapper.auto.WhiteListMapper;
import com.yunji.titanrtx.manager.service.auto.WhiteListService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-27 22:45
 * @Version 1.0
 */
@Service
public class WhiteListServiceImpl implements WhiteListService {
    @Resource
    WhiteListMapper mapper;

    @Override
    public List<FilterEntity> selectAll() {
        return mapper.selectAll();
    }

    @Override
    public int deleteById(Integer id) {
        return mapper.deleteById(id);
    }

    @Override
    public int insert(FilterEntity filterEntity) {
        return mapper.insert(filterEntity);
    }

    @Override
    public FilterEntity findById(Integer id) {
        return mapper.findById(id);
    }

    @Override
    public int update(FilterEntity filterEntity) {
        return mapper.update(filterEntity);
    }
}
