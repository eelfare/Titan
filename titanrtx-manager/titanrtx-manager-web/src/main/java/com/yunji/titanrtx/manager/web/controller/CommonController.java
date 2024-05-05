package com.yunji.titanrtx.manager.web.controller;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.domain.task.Pair;
import com.yunji.titanrtx.common.enums.Method;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.dubbo.DubboParamCaseBo;
import com.yunji.titanrtx.manager.dao.bos.http.HttpParamCaseBo;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.service.OutService;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.http.HttpStressService;
import com.yunji.titanrtx.plugin.dubbo.Invoker;
import com.yunji.titanrtx.plugin.dubbo.core.SyncInvoker;
import com.yunji.titanrtx.plugin.dubbo.support.GenericU;
import com.yunji.titanrtx.plugin.dubbo.support.RpcRequest;
import com.yunji.titanrtx.plugin.http.AHCBuildTool;
import com.yunji.titanrtx.plugin.http.AHCClientTool;
import com.yunji.titanrtx.plugin.http.HttpSyncClientTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

@Slf4j
@RestController
@RequestMapping("common/")
public class CommonController {

    @Value("${webSocketDomain:127.0.0.1:8080}")
    private String webSocketDomain;

    @Resource
    private HttpSceneService sceneService;

    @Resource
    private OutService outService;

    /**
     * 使用 AHC 进行 http 请求
     */
    @RequestMapping("doHttpParamCase.do")
    public RespMsg doHttpParamCaseAsync(@RequestBody HttpParamCaseBo bo) throws UnsupportedEncodingException {
        bo.build();
        Method method = bo.getLink().getMethod();
        String url = CommonU.buildFullUrl(bo.getLink().getProtocol().getMemo(), bo.getLink().getUrl());
        String requestParam = bo.getRequestParam();
        String requestHeader = bo.getRequestHeader();
        String contentType = bo.getLink().getContentType().getMemo();
        String charset = bo.getLink().getCharset().getMemo();
        Object responseText;
        if (Method.GET == method) {
            responseText = AHCClientTool.doGet(url, requestParam, requestHeader, contentType, charset);
        } else {
            responseText = AHCClientTool.doPost(url, requestParam, requestHeader, contentType, charset);
        }
        return RespMsg.respSuc(responseText);
    }

    @RequestMapping("doHttpParamCaseOld.do")
    public RespMsg doHttpParamCase(@RequestBody HttpParamCaseBo bo) throws UnsupportedEncodingException {
        bo.build();
        Method method = bo.getLink().getMethod();
        String url = CommonU.buildFullUrl(bo.getLink().getProtocol().getMemo(), bo.getLink().getUrl());
        String requestParam = bo.getRequestParam();
        String requestHeader = bo.getRequestHeader();
        String contentType = bo.getLink().getContentType().getMemo();
        String charset = bo.getLink().getCharset().getMemo();
        Object responseText;
        if (Method.GET == method) {
            responseText = HttpSyncClientTool.doGet(url, requestParam, requestHeader, contentType, charset);
        } else {
            responseText = HttpSyncClientTool.doPost(url, requestParam, requestHeader, contentType, charset);
        }
        return RespMsg.respSuc(responseText);
    }


    @RequestMapping("doDubboParamCase.do")
    public RespMsg doDubboParamCase(@RequestBody DubboParamCaseBo bo) {
        ServiceEntity serviceEntity = bo.getService();
        if (serviceEntity.getClusterAddress() == null) {
            String providerAddress =
                    outService
                            .getProviderAddress(serviceEntity.getApplicationName(), serviceEntity.getServiceName());

            if (StringUtils.isNotEmpty(providerAddress)) {
                serviceEntity.setClusterAddress(providerAddress);
            }
        }

        Invoker invoker = null;
        Object result;
        try {
            RpcRequest rpcRequest = GenericU.newBuild(
                    serviceEntity.getServiceName(),
                    serviceEntity.getMethodName(),
                    serviceEntity.getParamsType(),
                    bo.getRequestParam(),
                    serviceEntity.getAddress(),
                    serviceEntity.getRpcContent(),
                    serviceEntity.getClusterAddress());

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


    @RequestMapping("queryDomain.query")
    public RespMsg queryDomain() {
        return RespMsg.respSuc((Object) webSocketDomain);
    }

    @RequestMapping("getAlertInfo/{sceneId}")
    public RespMsg getAlertInfo(@PathVariable Integer sceneId) {
        Pair<String, String> alertInfo = sceneService.getAlertInfo(sceneId);
        log.info("webhook:{},isNull:{}, isEmpty:{}", alertInfo.getKey(), alertInfo.getKey() == null, StringUtils.isEmpty(alertInfo.getKey()));
        return RespMsg.respSuc(alertInfo.getKey());
    }

}
