package com.yunji.titanrtx.plugin.dubbo;

import com.yunji.titanrtx.plugin.dubbo.support.RpcRequest;

public interface Invoker {

    Object invoke(RpcRequest rpcRequest);

    void  destroy();

}
