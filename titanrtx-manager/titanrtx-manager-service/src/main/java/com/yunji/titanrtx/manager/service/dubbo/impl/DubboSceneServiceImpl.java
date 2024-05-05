package com.yunji.titanrtx.manager.service.dubbo.impl;

import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.dao.mapper.dubbo.DubboSceneMapper;
import com.yunji.titanrtx.manager.dao.mapper.dubbo.ServiceMapper;
import com.yunji.titanrtx.manager.service.dubbo.DubboSceneService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DubboSceneServiceImpl implements DubboSceneService {

    @Resource
    private DubboSceneMapper dubboSceneMapper;


    @Override
    public List<DubboSceneEntity> selectAll() {
        return dubboSceneMapper.selectAll();
    }

    @Override
    public DubboSceneEntity findById(Integer id) {
        return dubboSceneMapper.findById(id);
    }

    @Override
    public int deleteById(Integer id) {
        return dubboSceneMapper.deleteById(id);
    }

    @Override
    public int insert(DubboSceneEntity dubboSceneEntity) {
        return dubboSceneMapper.insert(dubboSceneEntity);
    }

    @Override
    public int update(DubboSceneEntity dubboSceneEntity) {
        return dubboSceneMapper.update(dubboSceneEntity);
    }

    @Override
    public List<DubboSceneEntity> searchScenes(String key) {
        return dubboSceneMapper.searchScenes(key);
    }

    @Override
    public List<Integer> selectSceneIds(String key) {
        return dubboSceneMapper.selectSceneIds(key);
    }

    @Override
    public int updateStatus(Integer id, int status) {
        return dubboSceneMapper.updateStatus(id,status);
    }
}
