package com.yunji.titanrtx.manager.service.dubbo;

import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;

import java.util.List;

public interface DubboServiceService {

    List<ServiceEntity> selectAll();

    List<ServiceEntity> selectAllOfParamStatus();

    int insert(ServiceEntity serviceEntity);

    ServiceEntity findById(Integer id);

    int update(ServiceEntity serviceEntity);

    List<ServiceEntity> searchService(String key);

    List<ServiceEntity> selectByIds(List<String> ids);

    int deleteById(Integer id);

    int updateDubboServiceOrderStatus(int id, int status);
}
