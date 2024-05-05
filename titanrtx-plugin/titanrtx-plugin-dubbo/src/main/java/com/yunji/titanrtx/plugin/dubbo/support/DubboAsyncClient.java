package com.yunji.titanrtx.plugin.dubbo.support;


import com.yunji.titanrtx.plugin.dubbo.FutureCallBack;
import com.yunji.titanrtx.plugin.dubbo.Invoker;
import com.yunji.titanrtx.plugin.dubbo.core.AsyncInvoker;
import org.apache.dubbo.common.utils.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DubboAsyncClient {

    private Invoker invoker  = new AsyncInvoker();

    private ExecutorService invokeThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("dubbo invoke threadPool"));;

    public void execute(RpcRequest rpcRequest, FutureCallBack<Object> futureCallBack){
        invokeThreadPool.execute(() -> {
                ((AsyncInvoker)invoker).invoke(rpcRequest,futureCallBack);
        });
    }


    public void close(){
        if (null != invokeThreadPool){
            invokeThreadPool.shutdownNow();
        }
        invoker.destroy();
    }


    public static DubboAsyncClient createAsyncClient(){
        return new DubboAsyncClient();
    }

}
