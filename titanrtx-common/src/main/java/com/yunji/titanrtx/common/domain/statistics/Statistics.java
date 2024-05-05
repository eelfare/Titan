package com.yunji.titanrtx.common.domain.statistics;

import com.yunji.titanrtx.common.enums.TaskType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
public class Statistics implements Serializable {

    private String taskNo;

    private String address;

    private TaskType taskType;

    private long requestTotal;

    private Map<Integer, StatisticsDetail> detailMap;

    private Map<Integer, String> urlIdMap;

    private Date startTime;

    private Date endTime;

    public StatisticsDetail getDetailStatistics(Integer linkId) {
        if (detailMap != null) {
            return detailMap.get(linkId);
        }
        return null;
    }

}
