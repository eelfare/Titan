package com.yunji.titanrtx.manager.service.system.impl;

import com.yunji.titanrtx.manager.dao.entity.system.OpsLogEntity;
import com.yunji.titanrtx.manager.dao.mapper.system.OpsLogMapper;
import com.yunji.titanrtx.manager.service.system.OpsLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OpsLogServiceImpl implements OpsLogService {

    @Resource
    private OpsLogMapper opsLogMapper;



    @Override
    public int insert(OpsLogEntity opsLogEntity) {
        return opsLogMapper.insert(opsLogEntity);
    }

    @Override
    public OpsLogEntity findById(Integer id) {
        return opsLogMapper.findById(id);
    }

    @Override
    public List<OpsLogEntity> selectAll() {
        return opsLogMapper.selectAll();
    }

    @Override
    public int deleteById(Integer id) {
        return opsLogMapper.deleteById(id);
    }

    @Override
    public List<OpsLogEntity> selectByUserName(String key) {
        return opsLogMapper.selectByUserName(key);
    }

}
