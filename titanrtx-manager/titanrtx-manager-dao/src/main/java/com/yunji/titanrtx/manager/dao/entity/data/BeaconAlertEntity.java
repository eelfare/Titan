package com.yunji.titanrtx.manager.dao.entity.data;

import lombok.Data;

/**
 * beacon 告警模型
 *
 * @author leihz
 * @since 2020-05-11 2:57 下午
 */
@Data
public class BeaconAlertEntity {

    private String alertId;

    private String alertTime;

    private String msg;
}
