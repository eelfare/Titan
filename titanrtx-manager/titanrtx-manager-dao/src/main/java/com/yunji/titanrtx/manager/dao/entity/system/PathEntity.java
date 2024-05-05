package com.yunji.titanrtx.manager.dao.entity.system;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PathEntity extends BaseEntity {

    private String name;

    private String uri;


}
