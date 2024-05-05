package com.yunji.titanrtx.manager.dao.bos.dubbo;


import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import lombok.Data;

@Data
public class DubboParamCaseBo {

    private ServiceEntity service;

    private String requestParam;

}
