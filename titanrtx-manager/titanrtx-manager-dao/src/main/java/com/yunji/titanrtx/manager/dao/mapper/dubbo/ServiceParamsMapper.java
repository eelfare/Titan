package com.yunji.titanrtx.manager.dao.mapper.dubbo;

import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceParamsEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceParamsMapper {

    List<Integer> selectAllIdByServiceId(Integer serviceId);

    int deleteById(Integer id);

    int deleteAllByServiceId(Integer serviceId);

    int insert(ServiceParamsEntity paramsEntity);

    ServiceParamsEntity findById(Integer id);

    int update(ServiceParamsEntity paramsEntity);

    void batchInsertList(@Param("paramsEntities") List<ServiceParamsEntity> paramsEntities);

    List<ServiceParamsEntity> selectByServiceId(Integer serviceId);

    int findTotalRecordsByServiceId(int dubboServiceId);

    Integer findMaxIdByServiceId(int dubboServiceId);

    int findOrdersById(int id);
}
