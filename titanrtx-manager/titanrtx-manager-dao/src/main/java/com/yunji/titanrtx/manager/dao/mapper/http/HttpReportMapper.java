package com.yunji.titanrtx.manager.dao.mapper.http;

import com.yunji.titanrtx.manager.dao.entity.http.HttpReportEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HttpReportMapper {

    int insert(HttpReportEntity httpReportEntity);

    List<HttpReportEntity> selectBySceneId(Integer sceneId);

    HttpReportEntity findById(Integer id);

    List<HttpReportEntity> selectAll();

    int deleteById(Integer id);

    int count();

    List<HttpReportEntity> selectByIds(List<String> reportIds);
}
