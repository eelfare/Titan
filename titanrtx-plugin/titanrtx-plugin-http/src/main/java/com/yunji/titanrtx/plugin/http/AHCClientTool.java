package com.yunji.titanrtx.plugin.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Async Http Client(AHC) Client Tool.
 */
@Slf4j
public class AHCClientTool {

    private static final AsyncHttpClient asyncHttpClient;
    private static final String CHARSET = "UTF-8";

    static {
        asyncHttpClient = Dsl.asyncHttpClient(
                Dsl.config()
                        .setMaxConnections(AHCBuildTool.ASYNC_MAX_CONNECT_NUM)
                        .setKeepAlive(true)
                        .setConnectTimeout(HttpBuildTool.CONNECT_TIMEOUT)
                        .setRequestTimeout(HttpBuildTool.REQUEST_TIMEOUT)
        );
    }

    public static String doGet(String url, String params) {
        return doGet(url, params, null);
    }

    public static String doGet(String url, String params, String headers) {
        return doGet(url, params, headers, CHARSET);
    }

    public static String doGet(String url, String params, String headers, String charset) {
        return doGet(url, params, headers, HttpBuildTool.HttpContent.JSON.getMemo(), charset);
    }

    public static String doGet(String url, String params, String headers, String contentType, String charset) {
        Request httpGet = AHCBuildTool.buildHttpGet(url, params, headers, contentType, charset);
        return doAction(httpGet);
    }


    public static String doPost(String url, String params, String headers, String contentType, String charset) {
        Request httpPost;

        if (StringUtils.equalsIgnoreCase(HttpBuildTool.HttpContent.XWWWFORMURLENCODED.getMemo(), contentType)) {
            httpPost = AHCBuildTool.buildHttpPostForm(url, params, headers, charset);
        } else {
            httpPost = AHCBuildTool.buildHttpPost(url, params, headers, contentType, charset);
        }
        return doAction(httpPost);
    }


    public static String doAction(Request request) {
        String result;
        if (asyncHttpClient != null && !asyncHttpClient.isClosed()) {
            //Send period begin.
            try {
                CompletableFuture<Response> responseFuture = asyncHttpClient
                        .executeRequest(request)
                        .toCompletableFuture();

                result = responseFuture.get().getResponseBody(StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error(e.getMessage());
                result = e.getMessage();
            }
            return result;
        }

        return "AsyncHttpClient is closed.";
    }
}
