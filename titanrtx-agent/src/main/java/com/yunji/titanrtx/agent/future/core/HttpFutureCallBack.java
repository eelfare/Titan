package com.yunji.titanrtx.agent.future.core;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.resp.RespCodeOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

@Slf4j
public class HttpFutureCallBack extends AbstractCallBack<HttpFutureCallBack, HttpResponse> implements FutureCallback<HttpResponse> {

    public HttpFutureCallBack(boolean openAgentCollectQps) {
        this.openAgentCollectQps = openAgentCollectQps;
    }

    @Override
    protected int doLogic(HttpResponse response) throws Exception {
//        YunJiRespDomain bo = JSON.parseObject(IOUtils.toString(response.getEntity().getContent(), GlobalConstants.URL_DECODER), YunJiRespDomain.class);
//        return bo.getErrorCode();
        return RespCodeOperator.getRespCode(IOUtils.toString(response.getEntity().getContent(), GlobalConstants.URL_DECODER));
    }


    @Override
    public void returnObject() {
        if (pool != null && !pool.isClosed()) {
            pool.returnObject(this);
        }
    }

}
