package com.yunji.titanrtx.manager.dao.bos.system;

import lombok.Data;

@Data
public class OpsLogBo {

    private Integer id;

    private Integer userId;

    private Integer permissionId;

    private String userName;

    private String pathName;

    private String params;

}
