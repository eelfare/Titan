package com.yunji.titanrtx.manager.dao.bos.system;

import com.yunji.titanrtx.manager.dao.entity.system.PathEntity;
import com.yunji.titanrtx.manager.dao.entity.system.UserEntity;
import lombok.Data;

import java.util.List;

@Data
public class UserBo {

    private UserEntity user;

    private List<PathEntity> hasPermission;

    private List<PathEntity> extraPermissions;
}
