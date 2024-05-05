package com.yunji.titanrtx.plugin.dubbo.core;

import com.yunji.titanrtx.plugin.dubbo.FutureCallBack;
import com.yunji.titanrtx.plugin.dubbo.support.RpcRequest;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.concurrent.CompletableFuture;

public class AsyncInvoker extends AbstractInvoker {

    public void invoke(RpcRequest rpcRequest, FutureCallBack<Object> callBack) {
        try {
            invoke(rpcRequest);
            callBack.start();
            CompletableFuture<Object> future = RpcContext.getContext().getCompletableFuture();
            future.whenComplete((retValue, exception) -> {
                if (exception == null) {
                    callBack.completed(retValue);
                } else {
                    callBack.failed(new RpcException(exception));
                }
            });
        } catch (Exception e) {
            callBack.start();
            callBack.failed(new RpcException(e));
        }
    }

    public CompletableFuture<Object> invoke2(RpcRequest rpcRequest) {
        invoke(rpcRequest);
        return RpcContext.getContext().getCompletableFuture();
    }

    @Override
    protected void genericConfig(ReferenceConfig<GenericService> generic) {
        generic.setAsync(true);
    }

    @Override
    protected String isAsync() {
        return Boolean.TRUE.toString();
    }
}
