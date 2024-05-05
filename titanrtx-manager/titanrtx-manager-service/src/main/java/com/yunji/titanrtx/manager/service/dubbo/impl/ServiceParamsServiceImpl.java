package com.yunji.titanrtx.manager.service.dubbo.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceParamsEntity;
import com.yunji.titanrtx.manager.dao.mapper.dubbo.ServiceParamsMapper;
import com.yunji.titanrtx.manager.service.dubbo.ServiceParamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class ServiceParamsServiceImpl implements ServiceParamsService {


    @Resource
    private ServiceParamsMapper serviceParamsMapper;


    @Override
    public List<Integer> selectAllIdByServiceId(Integer serviceId) {
        return serviceParamsMapper.selectAllIdByServiceId(serviceId);
    }

    @Override
    public int deleteById(Integer id) {
        return serviceParamsMapper.deleteById(id);
    }

    @Override
    public int deleteAllByServiceId(Integer serviceId) {
        return serviceParamsMapper.deleteAllByServiceId(serviceId);
    }

    @Override
    public int insert(ServiceParamsEntity paramsEntity) {
        return serviceParamsMapper.insert(paramsEntity);
    }

    @Override
    public ServiceParamsEntity findById(Integer id) {
        return serviceParamsMapper.findById(id);
    }

    @Override
    public int update(ServiceParamsEntity paramsEntity) {
        return serviceParamsMapper.update(paramsEntity);
    }

    @Override
    public PageInfo<ServiceParamsEntity> selectByServiceId(Integer serviceId, Integer currentPage) {
        PageHelper.startPage(currentPage, GlobalConstants.GLOBAL_PARAMS_PAGE_SIZE);
        return new PageInfo<>(serviceParamsMapper.selectByServiceId(serviceId));
    }

    @Override
    public int findTotalRecordsByServiceId(int dubboServiceId) {
        return serviceParamsMapper.findTotalRecordsByServiceId(dubboServiceId);
    }

    @Override
    public void batchInsertList(List<ServiceParamsEntity> paramsEntities) {
        serviceParamsMapper.batchInsertList(paramsEntities);
    }

    @Override
    public boolean checkParamIsOrder(int dubboServiceId, int lastCount) {
        Integer maxId = serviceParamsMapper.findMaxIdByServiceId(dubboServiceId);
        if (maxId == null) {
            log.warn("DUBBO 链路{} 的参数为空,忽略排序.", dubboServiceId);
            return true;
        }
        int orders = serviceParamsMapper.findOrdersById(maxId);

        if (orders == lastCount) {
            return true;
        }
        log.warn("DUBBO 链路{},maxId:{},对应orders:{}, lastCount:{} 不相等，检测失败.",
                dubboServiceId, maxId, orders, lastCount);
        return false;
    }

    @Override
    public boolean checkNullParamsOrder(int dubboServiceId, int lastCount, List<Integer> nullParamIds) {
        Integer maxId = serviceParamsMapper.findMaxIdByServiceId(dubboServiceId);
        if (maxId == null) {
            nullParamIds.add(dubboServiceId);
            return true;
        }
        int orders = serviceParamsMapper.findOrdersById(maxId);
        if (orders == lastCount) {
            return true;
        }
        log.warn("链路{},maxId:{},对应orders:{}, lastCount:{} 不相等，检测失败.",
                dubboServiceId, maxId, orders, lastCount);
        return false;
    }
}
