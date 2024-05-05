package com.yunji.titanrtx.manager.service;

import com.alibaba.fastjson.JSONException;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.service.support.CommonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Denim.leihz 2019-11-13 6:47 PM
 */
@Service
public class OutServiceImpl implements OutService {

    @Value("${dubbo.baseUrl}")
    private String baseUrl;
    @Value("${test.dubbo.baseUrl:http://dubboadmin.test1.sh.tx.yunjiglobal.com/out/}")
    private String testBaseUrl;

    @Value("${secretKey}")
    private String secretKey;

    @Value("${dubbo.providers}")
    private String providers;


    @Value("${dubbo.serviceUrls}")
    private String serviceUrls;

    @Value("${dubbo.serviceMethods}")
    private String serviceMethods;


    @Value("${dubbo.serviceAddress}")
    private String serviceAddress;

    @Value("${dubbo.providerAddress:providerAddress.query}")
    private String providerAddress;

    @Override
    public RespMsg providers() {
        return doInvoker(providers, "");
    }

    @Override
    public RespMsg serviceUrls(String application) {
        return doInvoker(serviceUrls, "&application=" + application);
    }

    @Override
    public RespMsg serviceMethods(String service) {
        return doInvoker(serviceMethods, "&service=" + service);
    }

    @Override
    public RespMsg serviceAddress(String service) {
        return doInvoker(serviceAddress, "&service=" + service);
    }

    @Override
    public String getProviderAddress(String application, String service) {
        try {
            return (String) CommonUtils
                    .httpGet(baseUrl + providerAddress,
                            "key=" + secretKey + "&application=" + application + "&service=" + service
                    ).getData();
        } catch (JSONException e) {
            return null;
        }
    }


    @Override
    public String getTestProviderAddress(String application, String service) {
        try {
            return (String) CommonUtils
                    .httpGet(testBaseUrl + providerAddress,
                            "key=" + secretKey + "&application=" + application + "&service=" + service
                    ).getData();
        } catch (JSONException e) {
            return null;
        }
    }

    private RespMsg doInvoker(String partUrl, String params) {
        try {
            return CommonUtils.httpGet(baseUrl + partUrl, "key=" + secretKey + params);
        } catch (JSONException e) {
            return RespMsg.respSuc();
        }
    }
}
