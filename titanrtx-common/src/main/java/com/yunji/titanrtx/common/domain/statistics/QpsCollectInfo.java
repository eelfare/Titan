package com.yunji.titanrtx.common.domain.statistics;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-10-29 12:22
 * @Version 1.0
 */
@Data
public class QpsCollectInfo implements Serializable {
    Date collectTime;
    int count2xx;
    int count3xx;
    int count4xx;
    int count5xx;
    int countOther;
    int countExpired;
    int countReceived;
    int countSend;

    public QpsCollectInfo() {
        collectTime = new Date();
    }
}
