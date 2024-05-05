package com.yunji.titanrtx.plugin.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HttpBuildTool {

    public static final int SOCKET_TIMEOUT = 10000;
    public static final int CONNECT_TIMEOUT = 1000;
    //    public static final int REQUEST_TIMEOUT = 10000;
    public static final int REQUEST_TIMEOUT = 5000;

    public static final int MAX_CONNECT_NUM = 10000;
    public static final int MAX_PER_ROUTE = 10000;

    private static final String HEADER_PAIR_SEGMENT = "&&&";
    private static final String PARAMS_PAIR_SEGMENT = "=";
    private static final String PARAMS_SEGMENT = "&";


    static Header[] parseRequestHeader(String headerParam) {
        if (StringUtils.isEmpty(headerParam)) return null;
        String[] headParamsPair = headerParam.split(HEADER_PAIR_SEGMENT);
        Header[] headers = new Header[headParamsPair.length];
        for (int i = 0; i < headParamsPair.length; i++) {
            String headPair = headParamsPair[i];
            if (StringUtils.isNotEmpty(headPair)) {
                String[] pair = headPair.split(PARAMS_PAIR_SEGMENT, 2);
                if (pair.length == 2) {
                    Header header = new BasicHeader(pair[0].trim(), pair[1].trim());
                    headers[i] = header;
                }
            }
        }
        return headers;
    }


    static Map<String, String> parseRequestParamsMap(String params) {
        Map<String, String> paramsMap = new HashMap<>();
        if (StringUtils.isEmpty(params)) return paramsMap;
        String[] paramsPair = params.split(PARAMS_SEGMENT);
        for (String keyVal : paramsPair) {
            if (StringUtils.isNotEmpty(keyVal)) {
                String[] keyValPair = keyVal.split(PARAMS_PAIR_SEGMENT, 2);
                if (keyValPair.length == 2) {
                    paramsMap.put(keyValPair[0].trim(), keyValPair[1].trim());
                }
            }
        }
        return paramsMap;
    }

    static String getRequestString(String url, String params, String charset) {
        Map<String, String> requestParamsMap = parseRequestParamsMap(params);
        if (requestParamsMap != null && requestParamsMap.size() > 0) {
            List<NameValuePair> pairs = new ArrayList<>(requestParamsMap.size());
            for (Map.Entry<String, String> entry : requestParamsMap.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            try {
                url += url.contains("?") ? "&" +
                        EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset))
                        : "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }
        return url;
    }


    public static HttpUriRequest buildHttpPost(String url, String params, String headers, String contentType, String charset) {
        HttpPost httpPost = new HttpPost(url);
        Header[] requestHeader = parseRequestHeader(headers);
        if (requestHeader != null) {
            for (Header header : requestHeader) {
                httpPost.setHeader(header);
            }
        }
        try {
            httpPost.setEntity(new StringEntity(params, contentType, charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return httpPost;
    }


    public static HttpUriRequest buildHttpPostForm(String url, String params, String headers, String charset) {
        Map<String, String> paramsMap = parseRequestParamsMap(params);
        List<NameValuePair> pairs = null;
        if (params != null && !params.isEmpty()) {
            pairs = new ArrayList<>(paramsMap.size());
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
        }
        HttpPost httpPost = new HttpPost(url);
        ;

        Header[] requestHeader = parseRequestHeader(headers);
        if (requestHeader != null) {
            for (Header header : requestHeader) {
                httpPost.setHeader(header);
            }
        }
        if (pairs != null && pairs.size() > 0) {
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();

            }
        }
        return httpPost;
    }


    public static HttpUriRequest buildHttpGet(String url, String params, String headers, String contentType, String charset) {
        url = getRequestString(url, params, charset);
        HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("Content-Type", contentType);
        Header[] requestHeader = parseRequestHeader(headers);
        if (requestHeader != null) {
            for (Header header : requestHeader) {
                httpGet.setHeader(header);
            }
        }
        return httpGet;
    }


    public enum HttpContent {
        XWWWFORMURLENCODED("application/x-www-form-urlencoded"),
        RAW("raw"),
        BINARY("binary"),
        Text("text/pain"),
        JSON("application/json"),
        JAVASCRIPT("application/javascript"),
        XML("application/xml"),
        HTML("text/html");

        private String memo;

        HttpContent(String memo) {
            this.memo = memo;
        }

        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }
    }
}
