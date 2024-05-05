package com.yunji.titanrtx.manager.service.http;

import com.yunji.titanrtx.manager.dao.entity.http.HttpBaseLineEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;

import java.util.List;

public interface HttpBaseLineService {

    int insert(HttpBaseLineEntity httpBaseLinkEntity);

    int update(HttpBaseLineEntity httpBaseLinkEntity);

    HttpBaseLineEntity selectBySceneIdAndLinkId(Integer sceneId, Integer linkId);

    List<HttpBaseLineEntity> selectBySceneId(Integer sceneId);

}
