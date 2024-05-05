package com.yunji.titanrtx.plugin.dubbo.core;

import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;

public class SyncInvoker extends AbstractInvoker {


    @Override
    protected void genericConfig(ReferenceConfig<GenericService> generic) {

    }

    @Override
    protected String isAsync() {
        return Boolean.FALSE.toString();
    }
}
