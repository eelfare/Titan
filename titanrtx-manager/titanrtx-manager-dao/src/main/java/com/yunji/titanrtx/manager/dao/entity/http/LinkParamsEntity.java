package com.yunji.titanrtx.manager.dao.entity.http;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LinkParamsEntity extends BaseEntity {

    private Integer linkId;

    private String param;

    public LinkParamsEntity() {
    }

    public LinkParamsEntity(int linkId, String param) {
        this.linkId = linkId;
        this.param = param;
    }


}
