package com.yunji.titanrtx.manager.service.report.dto;

import lombok.Data;

import java.util.List;

/**
 * 链路监控 monitoring 时封装的链路请求统计数据信息.
 */
@Data
public class LinkStatisticsDTO {
    /**
     * id
     */
    private int linkId;
    /**
     * url
     */
    private String linkUrl;
    /**
     * 1000
     */
    private long requestTotal;
    /**
     * 900
     */
    private long requestSuccess;
    /**
     * code: 0
     */
    private long bizSuccessCode;

    /**
     * code:1000
     */
    private long bizErrorCode;

    /**
     * 1
     */
    private long bizOtherCode;

    /**
     * 请求内容
     */
    private List<String> retContents;
}
