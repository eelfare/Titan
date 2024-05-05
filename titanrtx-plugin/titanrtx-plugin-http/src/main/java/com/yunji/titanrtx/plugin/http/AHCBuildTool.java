package com.yunji.titanrtx.plugin.http;

import com.yunji.titanrtx.plugin.http.tools.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import java.util.Map;

import static com.yunji.titanrtx.plugin.http.HttpBuildTool.getRequestString;
import static com.yunji.titanrtx.plugin.http.HttpBuildTool.parseRequestParamsMap;

/**
 * Build Async Http Client(AHC) Tool.
 */
@Slf4j
public class AHCBuildTool {
    private static final String HEADER_PAIR_SEGMENT = "&&&";
    private static final String PARAMS_PAIR_SEGMENT = "=";

    //    public static final int ASYNC_MAX_CONNECT_NUM = 2000;
    public static final int ASYNC_MAX_CONNECT_NUM = 5000;

    public static Request buildHttpGet(String url, String params, String headers, String contentType, String charset) {
        url = getRequestString(url, params, charset);
//        log.info("url = {}", url);
        RequestBuilder builder = Dsl.get(url);
        builder.setHeader("Content-Type", contentType);

        Header[] requestHeader = parseRequestHeader(headers);
        if (requestHeader != null) {
            for (Header header : requestHeader) {
                builder.setHeader(header.getKey(), header.getValue());
            }
        }
        return builder.build();
    }


    public static Request buildHttpPostForm(String url, String params, String headers, String charset) {
        RequestBuilder httpPost = Dsl.post(url);

        Map<String, String> paramsMap = parseRequestParamsMap(params);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    httpPost.addFormParam(entry.getKey(), entry.getValue());//
                }
            }
        }


        Header[] requestHeader = parseRequestHeader(headers);
        if (requestHeader != null) {
            for (Header header : requestHeader) {//
                httpPost.setHeader(header.getKey(), header.getValue());
            }
        }
        return httpPost.build();
    }

    public static Request buildHttpPost(String url, String params, String headers, String contentType, String charset) {
        RequestBuilder builder = Dsl.post(url);
        builder.setHeader("Content-Type", contentType);
        builder.setBody(params);

        Header[] requestHeader = parseRequestHeader(headers);
        if (requestHeader != null) {
            for (Header header : requestHeader) {
                builder.setHeader(header.getKey(), header.getValue());
            }
        }

        return builder.build();
    }


    private static Header[] parseRequestHeader(String headerParam) {
        if (StringUtils.isEmpty(headerParam)) return null;
        String[] headParamsPair = headerParam.split(HEADER_PAIR_SEGMENT);
        Header[] headers = new Header[headParamsPair.length];
        for (int i = 0; i < headParamsPair.length; i++) {
            String headPair = headParamsPair[i];
            if (StringUtils.isNotEmpty(headPair)) {
                String[] pair = headPair.split(PARAMS_PAIR_SEGMENT, 2);
                if (pair.length == 2) {
                    Header header = new Header(pair[0].trim(), pair[1].trim());
                    headers[i] = header;
                }
            }
        }
        return headers;
    }

}
