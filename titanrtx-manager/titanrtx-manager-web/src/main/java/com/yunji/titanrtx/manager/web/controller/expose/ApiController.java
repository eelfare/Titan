package com.yunji.titanrtx.manager.web.controller.expose;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.bos.dubbo.RemoteDubboRequestBO;
import com.yunji.titanrtx.manager.service.OutService;
import com.yunji.titanrtx.manager.web.controller.http.HttpSceneController;
import com.yunji.titanrtx.plugin.dubbo.Invoker;
import com.yunji.titanrtx.plugin.dubbo.core.SyncInvoker;
import com.yunji.titanrtx.plugin.dubbo.support.GenericU;
import com.yunji.titanrtx.plugin.dubbo.support.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @program: titanrtx
 * @description: 暴露给其他引用使用的API
 * @author: Jingf.Pang
 * @create: 2020-09-28 14:36
 **/
@RestController
@RequestMapping("expose/api")
@Slf4j
public class ApiController {
    @Resource
    HttpSceneController httpSceneController;
    @Resource
    private OutService outService;


    /**
     * 非生产环境所提供的外部压测api
     *
     * @param id
     * @return
     */
    @RequestMapping("stress")
    public RespMsg stress(Integer id) {
        if (id == null) {
            return RespMsg.respErr();
        }
        return httpSceneController.start(id);
    }

    /**
     * 远程dubbo执行api (只用于test1环境)
     *
     * @return
     */
    @RequestMapping("remoteDubboPerform")
    public RespMsg remoteDubboPerform(@RequestBody RemoteDubboRequestBO remoteDubboRequestBO) {
        if (StringUtils.isBlank(remoteDubboRequestBO.getApp())) {
            return RespMsg.respErr("app:应用名不能为空");
        }
        if (StringUtils.isBlank(remoteDubboRequestBO.getService())) {
            return RespMsg.respErr("service:服务名不能为空");
        }
        if (StringUtils.isBlank(remoteDubboRequestBO.getMethod())) {
            return RespMsg.respErr("method:方法名不能为空");
        }
        String providerAddress =
                outService
                        .getTestProviderAddress(remoteDubboRequestBO.getApp(), remoteDubboRequestBO.getService());
        log.info("provider address: " + providerAddress);
        Invoker invoker = null;
        Object result;
        try {
            RpcRequest rpcRequest = GenericU.newBuild(
                    remoteDubboRequestBO.getService(),
                    remoteDubboRequestBO.getMethod(),
                    remoteDubboRequestBO.getParamsTypes(),
                    remoteDubboRequestBO.getParams(),
                    GenericU.DUBBO_STRESS_TAG,
                    remoteDubboRequestBO.getRpcParams(),
                    providerAddress);

            invoker = new SyncInvoker();
            result = invoker.invoke(rpcRequest);
            log.info("\n req:{} \n rsp:{}", rpcRequest, result);
        } catch (Exception e) {
            e.printStackTrace();
            return RespMsg.respErr(e.getMessage());
        } finally {
            if (invoker != null) {
                invoker.destroy();
            }
        }
        return RespMsg.respSuc((Object) JSON.toJSONString(result));
    }
}
