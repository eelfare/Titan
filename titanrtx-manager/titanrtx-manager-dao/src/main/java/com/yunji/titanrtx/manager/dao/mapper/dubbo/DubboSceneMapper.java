package com.yunji.titanrtx.manager.dao.mapper.dubbo;

import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DubboSceneMapper {

    List<DubboSceneEntity> selectAll();

    DubboSceneEntity findById(Integer id);

    int deleteById(Integer id);

    int insert(DubboSceneEntity dubboSceneEntity);

    int update(DubboSceneEntity dubboSceneEntity);

    List<DubboSceneEntity> searchScenes(String key);

    List<Integer> selectSceneIds(String key);

    int updateStatus(@Param("id") Integer id, @Param("status") int status);

}
