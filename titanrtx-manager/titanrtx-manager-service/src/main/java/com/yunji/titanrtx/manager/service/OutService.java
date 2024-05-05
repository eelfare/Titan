package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.common.message.RespMsg;

/**
 * @author Denim.leihz 2019-11-13 6:47 PM
 */
public interface OutService {

    RespMsg providers();

    RespMsg serviceUrls(String application);

    RespMsg serviceMethods(String service);

    RespMsg serviceAddress(String service);

    String getProviderAddress(String application, String service);

    String getTestProviderAddress(String application, String service);

}
