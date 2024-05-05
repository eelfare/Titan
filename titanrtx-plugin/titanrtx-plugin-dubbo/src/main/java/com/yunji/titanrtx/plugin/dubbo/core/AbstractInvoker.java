package com.yunji.titanrtx.plugin.dubbo.core;

import com.google.common.base.Splitter;
import com.yunji.titanrtx.plugin.dubbo.Invoker;
import com.yunji.titanrtx.plugin.dubbo.support.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractInvoker implements Invoker {

    private static final String DUBBO_PROTOCOL = "dubbo://";

    private static final Integer TIME_OUT = 10 * 1000;

    private Map<String, ReferenceConfig<GenericService>> genericRefMap = new ConcurrentHashMap<>();
    private Map<String, GenericService> genericMap = new ConcurrentHashMap<>();

    @Override
    public Object invoke(RpcRequest rpcRequest) {
//        Object result;
//        ReferenceConfig<GenericService> generic = generateGeneric(rpcRequest);
        GenericService generic = generateClusterGeneric(rpcRequest);
        attachment(rpcRequest.getRpcContent());
        try {
            return generic.$invoke(rpcRequest.getMethod(), rpcRequest.getParamsType(), rpcRequest.getParamsValue());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
//        return result;
    }


    @Override
    public void destroy() {
        genericRefMap.forEach((s, generic) -> generic.destroy());
        genericRefMap.clear();
        genericMap.clear();
    }

    /*private  ReferenceConfig<GenericService> generateGeneric(RpcRequest rpcRequest) {
        ReferenceConfig<GenericService> reference = genericMap.get(rpcRequest.getServiceName());
        if (reference == null){
            reference = new ReferenceConfig<>();
            reference.setInterface(rpcRequest.getServiceName());
            reference.setGeneric(true);
            reference.setUrl(DUBBO_PROTOCOL + rpcRequest.getAddress());
            reference.setTimeout(TIME_OUT);
            genericMap.putIfAbsent(rpcRequest.getServiceName(),reference);
        }
        return reference;
    }*/

    private GenericService generateClusterGeneric(RpcRequest rpcRequest) {
        GenericService genericService = genericMap.get(rpcRequest.getServiceName());
        if (genericService == null) {
            // 直连方式，不使用注册中心
            RegistryConfig registry = new RegistryConfig();
            registry.setAddress("N/A");

            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setRegistry(registry);
            reference.setInterface(rpcRequest.getServiceName());
            reference.setGeneric(true);
            reference.setTimeout(TIME_OUT);
            List<URL> urls = reference.toUrls();

            urls.addAll(buildUrls(rpcRequest));

            genericRefMap.putIfAbsent(rpcRequest.getServiceName(), reference);
            genericMap.putIfAbsent(rpcRequest.getServiceName(), reference.get());

            genericService = genericMap.get(rpcRequest.getServiceName());
        }
        return genericService;
    }

    private void attachment(String rpcContent) {
        if (StringUtils.isBlank(rpcContent)) return;
        Splitter.on("&").trimResults().omitEmptyStrings().splitToList(rpcContent).forEach(pair -> {
            List<String> kv = Splitter.on("=").trimResults().omitEmptyStrings().splitToList(pair);
            if (kv.size() == 2) {
                RpcContext.getContext().setAttachment(kv.get(0), kv.get(1));
            }
        });
    }

    private List<URL> buildUrls(RpcRequest request) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(Constants.ASYNC_KEY, isAsync());

        List<URL> urls = new ArrayList<>();

        if (request.isCluster()) {
            for (String address : request.getClusterAddress()) {
                String[] result = address.split(":");
                urls.add(new URL(
                                Constants.DUBBO, result[0],
                                Integer.parseInt(result[1]),
                                request.getServiceName(),
                                parameters
                        )
                );
            }
        } else {
            for (String address : request.getAddress()) {
                String[] result = address.split(":");
                urls.add(new URL(
                                Constants.DUBBO, result[0],
                                Integer.parseInt(result[1]),
                                request.getServiceName(),
                                parameters
                        )
                );
            }
        }

        return urls;
    }

    protected abstract void genericConfig(ReferenceConfig<GenericService> generic);


    protected abstract String isAsync();
}
