package com.yunji.titanrtx.manager.dao.entity.system;


import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OpsLogEntity extends BaseEntity {

    private Integer userId;

    private String  userName;

    private String  pathName;

    private  String params;


    public OpsLogEntity(Integer userId, String userName, String pathName, String params) {
        this.userId = userId;
        this.userName = userName;
        this.pathName = pathName;
        this.params = params;
    }
}
