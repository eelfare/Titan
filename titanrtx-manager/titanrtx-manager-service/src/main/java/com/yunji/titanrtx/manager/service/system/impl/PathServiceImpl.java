package com.yunji.titanrtx.manager.service.system.impl;

import com.yunji.titanrtx.manager.dao.entity.system.PathEntity;
import com.yunji.titanrtx.manager.dao.mapper.system.OpsLogMapper;
import com.yunji.titanrtx.manager.dao.mapper.system.PathMapper;
import com.yunji.titanrtx.manager.service.system.PathService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PathServiceImpl implements PathService {


    @Resource
    private PathMapper pathMapper;


    @Override
    public int insert(PathEntity pathEntity) {
        return pathMapper.insert(pathEntity);
    }

    @Override
    public PathEntity findByUriPath(String uri) {
        return pathMapper.findByUriPath(uri);
    }

    @Override
    public PathEntity findById(Integer id) {
        return pathMapper.findById(id);
    }

    @Override
    public List<PathEntity> selectAll() {
        return pathMapper.selectAll();
    }

    @Override
    public int deleteById(Integer id) {
        return pathMapper.deleteById(id);
    }
}
