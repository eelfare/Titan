package com.yunji.titanrtx.manager.service.dubbo;

import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;

import java.util.List;

public interface DubboSceneService {

    List<DubboSceneEntity> selectAll();

    DubboSceneEntity findById(Integer id);

    int deleteById(Integer id);

    int insert(DubboSceneEntity dubboSceneEntity);

    int update(DubboSceneEntity dubboSceneEntity);

    List<DubboSceneEntity> searchScenes(String key);

    List<Integer> selectSceneIds(String key);

    int updateStatus(Integer id, int status);

}
