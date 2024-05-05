package com.yunji.titanrtx.manager.service.system;

import com.yunji.titanrtx.manager.dao.entity.system.OpsLogEntity;

import java.util.List;

public interface OpsLogService {

    int insert(OpsLogEntity opsLogEntity);

    OpsLogEntity findById(Integer id);

    List<OpsLogEntity> selectAll();

    int deleteById(Integer id);

    List<OpsLogEntity> selectByUserName(String key);
}
