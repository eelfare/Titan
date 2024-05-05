package com.yunji.titanrtx.manager.web.controller.dubbo;


import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.service.OutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("dubbo/")
public class DubboForeignController {

    @Resource
    private OutService outService;

    @RequestMapping("providers.query")
    public RespMsg providers() {
        return outService.providers();
//        return doInvoker(providers,"");
    }


    @RequestMapping("serviceUrls.query")
    private RespMsg serviceUrls(String application) {
        return outService.serviceUrls(application);
//        return doInvoker(serviceUrls, "&application=" + application);
    }

    @RequestMapping("serviceMethods.query")
    private RespMsg serviceMethods(String service) {
        return outService.serviceMethods(service);
//        return doInvoker(serviceMethods, "&service=" + service);
    }

    @RequestMapping("serviceAddress.query")
    public RespMsg serviceAddress(String service) {
        return outService.serviceAddress(service);
//        return doInvoker(serviceAddress, "&service=" + service);
    }
}
