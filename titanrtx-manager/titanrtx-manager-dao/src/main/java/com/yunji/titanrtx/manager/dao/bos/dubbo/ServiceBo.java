package com.yunji.titanrtx.manager.dao.bos.dubbo;

import com.github.pagehelper.PageInfo;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceParamsEntity;
import lombok.Data;

@Data
public class ServiceBo {

    private ServiceEntity service;

    private PageInfo<ServiceParamsEntity> params;

}
