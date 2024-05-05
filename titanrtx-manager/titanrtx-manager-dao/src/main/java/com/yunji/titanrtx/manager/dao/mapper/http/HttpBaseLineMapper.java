package com.yunji.titanrtx.manager.dao.mapper.http;

import com.yunji.titanrtx.manager.dao.entity.http.HttpBaseLineEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 13/4/2020 10:56 上午
 * @Version 1.0
 */
@Mapper
public interface HttpBaseLineMapper {
    int insert(HttpBaseLineEntity httpBaseLinkEntity);

    int update(HttpBaseLineEntity httpBaseLinkEntity);

    HttpBaseLineEntity selectBySceneIdAndLinkId(@Param("sceneId") Integer sceneId, @Param("linkId") Integer linkId);

    List<HttpBaseLineEntity> selectBySceneId(Integer sceneId);
}
