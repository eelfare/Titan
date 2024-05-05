package com.yunji.titanrtx.manager.dao.entity.auto;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-27 22:46
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BlackGroupEntity extends BaseEntity {
    private String name;
}
