package com.yunji.titanrtx.manager.dao.mapper.http;

import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.SceneStressEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HttpSceneMapper {

    List<HttpSceneEntity> selectAll();

    int deleteById(Integer id);

    int insert(HttpSceneEntity httpSceneEntity);

    HttpSceneEntity findById(Integer id);

    List<HttpSceneEntity> findByName(String name);

    int update(HttpSceneEntity httpSceneEntity);

    int updateStatus(@Param("id") Integer id, @Param("status") int status);

    List<Integer> selectSceneIds(String key);

    List<HttpSceneEntity> searchScenes(String key);

    int count();

    void resetAll();

    List<SceneStressEntity> selectAllSceneStresses();
}
