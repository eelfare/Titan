package com.yunji.titanrtx.manager.dao.mapper.system;

import com.yunji.titanrtx.manager.dao.entity.system.OpsLogEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OpsLogMapper {

    int insert(OpsLogEntity opsLogEntity);

    OpsLogEntity findById(Integer id);

    List<OpsLogEntity> selectAll();

    int deleteById(Integer id);

    List<OpsLogEntity> selectByUserName(String key);
}
