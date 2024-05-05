package com.yunji.titanrtx.manager.service.http.impl;

import com.yunji.titanrtx.common.domain.task.Pair;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.SceneStressEntity;
import com.yunji.titanrtx.manager.dao.mapper.http.HttpSceneMapper;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class HttpSceneServiceImpl implements HttpSceneService {

    @Resource
    private HttpSceneMapper httpSceneMapper;


    @Override
    public List<HttpSceneEntity> selectAll() {
        return httpSceneMapper.selectAll();
    }

    @Override
    public int deleteById(Integer id) {
        return httpSceneMapper.deleteById(id);
    }

    @Override
    public int insert(HttpSceneEntity httpSceneEntity) {
        return httpSceneMapper.insert(httpSceneEntity);
    }

    @Override
    public HttpSceneEntity findById(Integer id) {
        return httpSceneMapper.findById(id);
    }

    @Override
    public List<HttpSceneEntity> findByName(String name) {
        return httpSceneMapper.findByName(name);
    }

    @Override
    public int update(HttpSceneEntity httpSceneEntity) {
        return httpSceneMapper.update(httpSceneEntity);
    }

    @Override
    public int updateStatus(Integer id, int status) {
        return httpSceneMapper.updateStatus(id, status);
    }

    @Override
    public List<Integer> selectSceneIds(String key) {
        return httpSceneMapper.selectSceneIds(key);
    }

    @Override
    public List<HttpSceneEntity> searchScenes(String key) {
        return httpSceneMapper.searchScenes(key);
    }

    @Override
    public int count() {
        return httpSceneMapper.count();
    }

    @Override
    public void resetAll() {
        httpSceneMapper.resetAll();
    }

    @Override
    public Pair<String, String> getAlertInfo(Integer id) {
        HttpSceneEntity entity = httpSceneMapper.findById(id);
        return new Pair<>(entity.getWebhook(), entity.getAlertThreshold());
    }

    @Override
    public  List<SceneStressEntity> selectAllSceneStresses() {
        return httpSceneMapper.selectAllSceneStresses();
    }
}
