package com.yunji.titanrtx.common.alarm;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * MessageSender
 *
 * @author leihz
 * @since 2020-08-12 10:06 上午
 */
@Slf4j
public class MessageSender {
    private static final Gson GSON = new Gson();

    public static final String BEACON_FEISHU_URL = "http://beacon.yunjiglobal.com/beacon_alert/send";

    public enum Type {
        MAINTAINER,
        AUTO_ALARM,
        FLOW_CREATOR,
        PERF_BASELINE,
        CUSTOMER
    }

    @Data
    @Builder
    public static class Feishu {
        private String title;
        private String text;
    }

    @Data
    @Builder
    public static class Beacon {
        private String alertId;
        private String msg;
    }

    public static String buildFsMessage(String msg, Type type) {
        return GSON.toJson(
                new MessageSender
                        .Feishu
                        .FeishuBuilder()
                        .text(msg)
                        .title("飞书告警信息[" + type + "]")
                        .build()
        );
    }


    public static String buildBeaconMessage(String msg, String alertId) {
        return GSON.toJson(
                new Beacon
                        .BeaconBuilder()
                        .msg(msg)
                        .alertId(alertId)
                        .build()
        );
    }

    /**
     * url1: 企业微信
     * url2: 飞书
     * <p>
     * 发送消息
     */
    public static void send(String url1, String url2, String msg, String alertId) {
        if (StringUtils.isNotEmpty(url2) && url2.startsWith("https://qyapi")) {
            url2 = BEACON_FEISHU_URL;
        }

        doSend(url1, msg, alertId);

        doSend(url2, msg, alertId);
    }

    /**
     * doSend
     */
    public static void doSend(String url, String msg, String alertId) {
        if (StringUtils.isNotEmpty(url)) {
            if (url.contains("feishu")) {
                msg = buildFsMessage(msg, null);
            }
            if (url.contains("beacon")) {
                msg = buildBeaconMessage(msg, alertId);
            }

            CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
            client.start();

            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-type", "application/json; charset=utf-8");
            StringEntity stringEntity = new StringEntity(msg, StandardCharsets.UTF_8);
            httpPost.setEntity(stringEntity);
            client.execute(httpPost, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse result) {
                    close(client);
                    log.info("发送告警消息成功，alertId:{}.", alertId);
                }

                @Override
                public void failed(Exception ex) {
                    close(client);
                    log.info("发送机器人消息失败,url:{},cause:{}", url, ex.getMessage());
                }

                @Override
                public void cancelled() {
                    close(client);
                }
            });
        }
    }

    private static void close(CloseableHttpAsyncClient client) {
        try {
            client.close();
        } catch (IOException ignored) {
        }
    }
}
