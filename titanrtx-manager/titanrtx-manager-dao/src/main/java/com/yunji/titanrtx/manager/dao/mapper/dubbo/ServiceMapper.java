package com.yunji.titanrtx.manager.dao.mapper.dubbo;

import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceMapper {

    List<ServiceEntity> selectAll();

    List<ServiceEntity> selectAllOfParamStatus();

    Integer insert(ServiceEntity serviceEntity);

    ServiceEntity findById(Integer id);

    int update(ServiceEntity serviceEntity);

    int deleteById(Integer id);

    List<ServiceEntity> searchService(String key);

    List<ServiceEntity> selectByIds(List<String> ids);


    int updateParamOrderStatus(@Param("id") int id, @Param("status") int status);
}
