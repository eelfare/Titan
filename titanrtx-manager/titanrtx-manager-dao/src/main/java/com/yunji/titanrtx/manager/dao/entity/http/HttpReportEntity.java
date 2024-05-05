package com.yunji.titanrtx.manager.dao.entity.http;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HttpReportEntity extends BaseEntity {

    private Integer sceneId;
    /**
     * 快照是压测报告和场景，链路的快照
     * 用于存储防止场景更改时带来的报告混杂性
     */
    private String snap;

}
