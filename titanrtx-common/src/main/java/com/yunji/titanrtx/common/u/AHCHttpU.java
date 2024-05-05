package com.yunji.titanrtx.common.u;

import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.HttpLink;
import com.yunji.titanrtx.common.enums.Content;
import com.yunji.titanrtx.common.enums.Method;
import com.yunji.titanrtx.plugin.http.AHCBuildTool;
import com.yunji.titanrtx.plugin.http.AHCClientTool;
import org.asynchttpclient.Request;

/**
 * AHCHttpU
 *
 * @author leihz
 * @since 2020-05-20 11:27 上午
 */
public class AHCHttpU {
    /**
     * 执行请求
     */
    public static String executeRequest(Request request) {
        return AHCClientTool.doAction(request);
    }

    /**
     * 根据 bullet 构造 Request 请求
     */
    public static Request buildRequestByBullet(HttpLink httpLink) {
        return buildRequest(httpLink, null);
    }

    public static Request buildRequest(HttpLink httpLink, String param) {
        Method method = httpLink.getMethod();

        if (param == null && httpLink.getParams().isEmpty()) {
            throw new RuntimeException("当前链路参数为空,linkId: " + httpLink.getId());
        }
        if (param == null) {
            param = httpLink.getParams().get(0);
        }

        String url = CommonU.buildFullUrl(httpLink.getProtocol().getMemo(), httpLink.getUrl());
        String params = CommonU.splitParams(param);
        String header = CommonU.splitHeader(param);
        String contentMemo = httpLink.getContentType().getMemo();
        String charsetMemo = httpLink.getCharset().getMemo();

        Request request;

        switch (method) {
            case GET:
                request = AHCBuildTool.buildHttpGet(url, params, header, contentMemo, charsetMemo);
                break;
            case POST:
                if (Content.XWWWFORMURLENCODED == httpLink.getContentType()) {
                    request = AHCBuildTool.buildHttpPostForm(url, params, header, charsetMemo);
                } else {
                    //
                    request = AHCBuildTool.buildHttpPost(url, params, header, contentMemo, charsetMemo);
                }
                break;
            default:
                throw new RuntimeException("暂不支持类型: " + method.name());
        }

        return request;
    }


}