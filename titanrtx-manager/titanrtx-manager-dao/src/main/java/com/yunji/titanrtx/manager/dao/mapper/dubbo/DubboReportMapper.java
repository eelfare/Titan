package com.yunji.titanrtx.manager.dao.mapper.dubbo;

import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboReportEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpReportEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DubboReportMapper {

    int insert(DubboReportEntity dubboReportEntity);

    List<DubboReportEntity> selectBySceneId(Integer sceneId);

    DubboReportEntity findById(Integer id);

    List<DubboReportEntity> selectAll();

    int deleteById(Integer id);

    int count();

    List<DubboReportEntity> selectByIds(List<String> reportIds);
}
