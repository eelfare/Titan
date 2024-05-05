package com.yunji.titanrtx.plugin.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class HttpSyncClientTool {

    private static final CloseableHttpClient httpClient;
    private static final String CHARSET = "UTF-8";

    static {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(HttpBuildTool.CONNECT_TIMEOUT).setSocketTimeout(HttpBuildTool.SOCKET_TIMEOUT).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    public static String doGet(String url,String params)  {
        return doGet(url,params, null);
    }

    public static String doGet(String url,String params, String headers) {
        return doGet(url,params, headers,CHARSET);
    }

    public static String doGet(String url,String params,String headers,String charset) {
        return doGet(url,params,headers, HttpBuildTool.HttpContent.JSON.getMemo(),charset);
    }

    public static String doGet(String url,String params, String headers,String contentType,String charset)  {
        HttpUriRequest httpGet = HttpBuildTool.buildHttpGet(url, params, headers, contentType, charset);
        return doAction(httpGet,charset);
    }


    public static String doPost(String url,String params,String headers,String contentType,String charset){
        HttpUriRequest httpPost;

        if (StringUtils.equalsIgnoreCase(HttpBuildTool.HttpContent.XWWWFORMURLENCODED.getMemo(),contentType)){
            httpPost = HttpBuildTool.buildHttpPostForm(url, params,headers,charset);
        }else{
            httpPost = HttpBuildTool.buildHttpPost(url, params, headers, contentType, charset);
        }
        return doAction(httpPost,charset);
    }


    private static String doAction(HttpUriRequest request,String charset) {
        CloseableHttpResponse response = null;
        String result = null;
        try {
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            log.error(e.getMessage());
            result = Arrays.toString(e.getStackTrace());
        }finally {
            try {
                if (response != null){
                    response.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return result;
    }


}
