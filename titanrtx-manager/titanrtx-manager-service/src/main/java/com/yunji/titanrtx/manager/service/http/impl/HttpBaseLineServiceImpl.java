package com.yunji.titanrtx.manager.service.http.impl;

import com.yunji.titanrtx.manager.dao.entity.http.HttpBaseLineEntity;
import com.yunji.titanrtx.manager.dao.mapper.http.HttpBaseLineMapper;
import com.yunji.titanrtx.manager.service.http.HttpBaseLineService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class HttpBaseLineServiceImpl implements HttpBaseLineService {
    @Resource
    HttpBaseLineMapper httpBaseLineMapper;
    @Override
    public int insert(HttpBaseLineEntity httpBaseLinkEntity) {
        return httpBaseLineMapper.insert(httpBaseLinkEntity);
    }

    @Override
    public int update(HttpBaseLineEntity httpBaseLinkEntity) {
        return httpBaseLineMapper.update(httpBaseLinkEntity);
    }

    @Override
    public HttpBaseLineEntity selectBySceneIdAndLinkId(Integer sceneId, Integer linkId) {
        return httpBaseLineMapper.selectBySceneIdAndLinkId(sceneId,linkId);
    }

    @Override
    public List<HttpBaseLineEntity> selectBySceneId(Integer sceneId) {
        return httpBaseLineMapper.selectBySceneId(sceneId);
    }
}
