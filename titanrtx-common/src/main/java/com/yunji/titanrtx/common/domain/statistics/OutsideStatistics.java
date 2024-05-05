package com.yunji.titanrtx.common.domain.statistics;

import lombok.Data;

/**
 * 外部统计数据 Bean
 *
 * @author leihuazhe
 * @since 2020.4.28
 */
@Data
public class OutsideStatistics {

    private Statistics statistics;

    private long startTime;

    private long endTime;

    public OutsideStatistics(Statistics statistics, long startTime, long endTime) {
        this.statistics = statistics;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public OutsideStatistics() {
    }
}
