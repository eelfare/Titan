package com.yunji.titanrtx.manager.service.dubbo;

import com.github.pagehelper.PageInfo;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceParamsEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;

import java.util.List;

public interface ServiceParamsService {

    List<Integer> selectAllIdByServiceId(Integer serviceId);

    int deleteById(Integer id);

    int deleteAllByServiceId(Integer serviceId);

    int insert(ServiceParamsEntity paramsEntity);

    ServiceParamsEntity findById(Integer id);

    int update(ServiceParamsEntity paramsEntity);

    PageInfo<ServiceParamsEntity> selectByServiceId(Integer serviceId, Integer currentPage);

    int findTotalRecordsByServiceId(int dubboServiceId);

    void batchInsertList(List<ServiceParamsEntity> paramsEntities);

    boolean checkParamIsOrder(int dubboServiceId, int lastCount);


    boolean checkNullParamsOrder(int dubboServiceId, int lastCount, List<Integer> nullParamIds);
}
