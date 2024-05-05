package com.yunji.titanrtx.manager.dao.entity.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LinkOrderEntity extends LinkEntity {
    private int count;
}
