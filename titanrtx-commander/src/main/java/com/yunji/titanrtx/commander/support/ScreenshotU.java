package com.yunji.titanrtx.commander.support;

/**
 * ScreenshotU
 *
 * @author leihz
 * @since 2020-06-01 6:31 下午
 */
public class ScreenshotU {

    private static final String ALL_QPS_URL
            = "https://grafana-tx.yunjiweidian.com/d/jvB5-cUZz/zong-qps-qu-shi-tu?orgId=1&refresh=5s";

    private static final String DUBBO_COST_ORDER
            = "https://grafana-tx.yunjiweidian.com/d/hdaIROZik/dubbohao-shi-pai-xing?orgId=1&refresh=10s";

    public static String getUrl(String type) {
        if ("all_qps".equals(type)) {
            return ALL_QPS_URL;
        }
        if ("dubbo_cost_order".equals(type))
            return DUBBO_COST_ORDER;

        throw new IllegalArgumentException("Not supported type: " + type);
    }

}
