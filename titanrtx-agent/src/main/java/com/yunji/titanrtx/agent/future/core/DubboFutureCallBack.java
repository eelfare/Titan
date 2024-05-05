package com.yunji.titanrtx.agent.future.core;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.resp.RespCodeOperator;
import com.yunji.titanrtx.plugin.dubbo.FutureCallBack;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DubboFutureCallBack extends AbstractCallBack<DubboFutureCallBack, Object> implements FutureCallBack<Object> {


    @Override
    protected int doLogic(Object o) {
//        YunJiRespDomain bo = JSON.parseObject(JSON.toJSONString(o), YunJiRespDomain.class);
//        return bo.getErrorCode();

        return RespCodeOperator.getRespCode(JSON.toJSONString(o));
    }


    @Override
    public void returnObject() {
        if (pool != null && !pool.isClosed()) {
            pool.returnObject(this);
        }
    }
}
