package com.yunji.titanrtx.manager.dao.mapper.system;

import com.yunji.titanrtx.manager.dao.entity.system.PathEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PathMapper {

    int insert(PathEntity pathEntity);

    PathEntity findByUriPath(String uri);

    PathEntity findById(Integer id);

    List<PathEntity> selectAll();

    int deleteById(Integer id);

}
