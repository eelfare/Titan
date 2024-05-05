package com.yunji.titanrtx.manager.service.common;

import com.yunji.titanrtx.manager.dao.entity.IncrementEntity;
import com.yunji.titanrtx.manager.dao.mapper.common.IncrementMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class IncrementServiceImpl implements IncrementService {

    @Resource
    private IncrementMapper incrementIDMapper;


    @Override
    public int incrementID() {
        IncrementEntity entity = new IncrementEntity();
        incrementIDMapper.incrementID(entity);
        return entity.getId();
    }


}
