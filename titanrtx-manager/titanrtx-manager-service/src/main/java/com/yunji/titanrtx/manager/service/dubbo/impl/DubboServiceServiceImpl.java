package com.yunji.titanrtx.manager.service.dubbo.impl;

import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.dao.mapper.dubbo.ServiceMapper;
import com.yunji.titanrtx.manager.dao.mapper.dubbo.ServiceParamsMapper;
import com.yunji.titanrtx.manager.service.dubbo.DubboServiceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DubboServiceServiceImpl implements DubboServiceService {


    @Resource
    private ServiceMapper serviceMapper;

    @Resource
    private ServiceParamsMapper serviceParamsMapper;


    @Override
    public List<ServiceEntity> selectAll() {
        return serviceMapper.selectAll();
    }

    @Override
    public List<ServiceEntity> selectAllOfParamStatus() {
        return serviceMapper.selectAllOfParamStatus();
    }


    @Override
    public int insert(ServiceEntity serviceEntity) {
        return serviceMapper.insert(serviceEntity);
    }

    @Override
    public ServiceEntity findById(Integer id) {
        return serviceMapper.findById(id);
    }

    @Override
    public int update(ServiceEntity serviceEntity) {
        return serviceMapper.update(serviceEntity);
    }

    @Override
    public List<ServiceEntity> searchService(String key) {
        return serviceMapper.searchService(key);
    }

    @Override
    public List<ServiceEntity> selectByIds(List<String> ids) {
        return serviceMapper.selectByIds(ids);
    }

    @Override
    public int deleteById(Integer id) {
        serviceParamsMapper.deleteAllByServiceId(id);
        return serviceMapper.deleteById(id);
    }

    @Override
    public int updateDubboServiceOrderStatus(int id, int status) {
        return serviceMapper.updateParamOrderStatus(id, status);
    }

}
