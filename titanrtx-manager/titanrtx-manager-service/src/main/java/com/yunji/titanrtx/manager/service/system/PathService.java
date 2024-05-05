package com.yunji.titanrtx.manager.service.system;

import com.yunji.titanrtx.manager.dao.entity.system.PathEntity;

import java.util.List;

public interface PathService {

    int insert(PathEntity pathEntity);

    PathEntity findByUriPath(String uri);

    PathEntity findById(Integer id);

    List<PathEntity> selectAll();

    int deleteById(Integer id);

}
