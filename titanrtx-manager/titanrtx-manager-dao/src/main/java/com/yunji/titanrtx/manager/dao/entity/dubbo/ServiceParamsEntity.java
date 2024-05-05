package com.yunji.titanrtx.manager.dao.entity.dubbo;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceParamsEntity extends BaseEntity {

    private int serviceId;

    private String param;


    public ServiceParamsEntity() {
    }

    public ServiceParamsEntity(int serviceId, String param) {
        this.serviceId = serviceId;
        this.param = param;
    }
}
