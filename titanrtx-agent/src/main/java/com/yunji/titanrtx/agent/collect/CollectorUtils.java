package com.yunji.titanrtx.agent.collect;

import com.yunji.titanrtx.common.GlobalConstants;

/**
 * @author Denim.leihz 2019-11-09 2:13 PM
 */
public class CollectorUtils {

    /**
     * 采集请求
     *
     * @param url
     */
    public static void collectRequest(boolean openAgentCollectQps, String url) {
        if (!openAgentCollectQps) {
            return;
        }
        Collector.doCollect(true, url, null);
    }

    /**
     * 采集成功返回
     * * @param responseCode
     *
     * @param url
     */
    public static void collectSuccessResponse(boolean openAgentCollectQps, int responseCode, String url) {
        if (!openAgentCollectQps) {
            return;
        }
        long[] response = new long[Collector.COLLECT_LIST_LENGTH];
        response[6] = 1;
        if (!Integer.toString(responseCode).startsWith("2")) {
            if (!Integer.toString(responseCode).startsWith("3")) {
                if (!Integer.toString(responseCode).startsWith("4")) {
                    if (!Integer.toString(responseCode).startsWith("5")) {
                        // 超时这里需要重新考量
                        if (responseCode == GlobalConstants.YUNJI_ERROR_CODE) {
                            response[5] = 1;
                        } else {
                            response[4] = 1;
                        }
                    } else {
                        response[3] = 1;
                    }
                } else {
                    response[2] = 1;
                }
            } else {
                response[1] = 1;
            }
        } else {
            response[0] = 1;
        }
        Collector.doCollect(false, url, response);
    }

    /**
     * 采集错误返回
     *
     * @param url
     */
    public static void collectErrorResponse(boolean openAgentCollectQps, String url, Throwable t) {
        if (!openAgentCollectQps) {
            return;
        }
        long[] response = new long[Collector.COLLECT_LIST_LENGTH];
        response[6] = 1;
        response[7] = 1;
        Collector.doCollect(false, url, response);
    }

    /**
     * 采集超时返回
     *
     * @param url
     */
    public static void collectTimeoutResponse(boolean openAgentCollectQps, String url) {
        if (!openAgentCollectQps) {
            return;
        }
        long[] response = new long[Collector.COLLECT_LIST_LENGTH];
        response[6] = 1;
        response[5] = 1;
        Collector.doCollect(false, url, response);
    }
}
