package com.yunji.titanrtx.manager.dao.bos.dubbo;

import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DubboSceneBo implements Serializable {

    private DubboSceneEntity dubboSceneEntity;

    private List<ServiceEntity> services;

}
