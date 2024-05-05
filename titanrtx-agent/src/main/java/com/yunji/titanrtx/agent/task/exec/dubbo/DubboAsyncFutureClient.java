package com.yunji.titanrtx.agent.task.exec.dubbo;


import com.yunji.titanrtx.plugin.dubbo.Invoker;
import com.yunji.titanrtx.plugin.dubbo.core.AsyncInvoker;
import com.yunji.titanrtx.plugin.dubbo.support.RpcRequest;

import java.util.concurrent.CompletableFuture;

public class DubboAsyncFutureClient {
    private Invoker invoker = new AsyncInvoker();

    public CompletableFuture<Object> execute(RpcRequest rpcRequest) {
        return ((AsyncInvoker) invoker).invoke2(rpcRequest);
    }

    public static DubboAsyncFutureClient createAsyncClient() {
        return new DubboAsyncFutureClient();
    }

    public void close() {
        invoker.destroy();
    }
}
