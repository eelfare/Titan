package com.yunji.titanrtx.manager.dao.entity.http;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 13/4/2020 10:51 上午
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpBaseLineEntity extends BaseEntity {
    private Integer sceneId;
    private Integer linkId;
    private String baseLine; // 性能基线指标（JSON）
}
