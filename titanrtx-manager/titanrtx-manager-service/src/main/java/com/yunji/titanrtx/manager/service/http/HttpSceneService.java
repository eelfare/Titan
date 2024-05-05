package com.yunji.titanrtx.manager.service.http;

import com.yunji.titanrtx.common.domain.task.Pair;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.SceneStressEntity;

import java.util.List;

public interface HttpSceneService {

    List<HttpSceneEntity> selectAll();

    int deleteById(Integer id);

    int insert(HttpSceneEntity httpSceneEntity);

    HttpSceneEntity findById(Integer id);

    List<HttpSceneEntity> findByName(String name);

    int update(HttpSceneEntity httpSceneEntity);

    int updateStatus(Integer id, int status);

    List<Integer> selectSceneIds(String key);

    List<HttpSceneEntity> searchScenes(String key);

    int count();

    void resetAll();

    Pair<String, String> getAlertInfo(Integer id);

    List<SceneStressEntity> selectAllSceneStresses();

}
