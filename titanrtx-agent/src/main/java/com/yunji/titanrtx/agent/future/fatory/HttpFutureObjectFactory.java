package com.yunji.titanrtx.agent.future.fatory;

import com.yunji.titanrtx.agent.future.core.HttpFutureCallBack;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class HttpFutureObjectFactory implements PooledObjectFactory<HttpFutureCallBack> {

    boolean openAgentCollectQps;

    public HttpFutureObjectFactory(boolean openAgentCollectQps) {
        this.openAgentCollectQps = openAgentCollectQps;
    }

    @Override
    public PooledObject<HttpFutureCallBack> makeObject() {
        HttpFutureCallBack future = new HttpFutureCallBack(openAgentCollectQps);
        return new DefaultPooledObject<>(future);
    }

    @Override
    public void destroyObject(PooledObject<HttpFutureCallBack> p) {

    }

    @Override
    public boolean validateObject(PooledObject<HttpFutureCallBack> p) {
        return false;
    }

    @Override
    public void activateObject(PooledObject<HttpFutureCallBack> p) {

    }

    @Override
    public void passivateObject(PooledObject<HttpFutureCallBack> p) {

    }
}
