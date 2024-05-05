package com.yunji.titanrtx.agent.future.fatory;

import com.yunji.titanrtx.agent.future.core.DubboFutureCallBack;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class DubboFutureObjectFactory implements PooledObjectFactory<DubboFutureCallBack> {

    @Override
    public PooledObject<DubboFutureCallBack> makeObject() {
        DubboFutureCallBack future = new DubboFutureCallBack();
        return new DefaultPooledObject<>(future);
    }

    @Override
    public void destroyObject(PooledObject<DubboFutureCallBack> p) {

    }

    @Override
    public boolean validateObject(PooledObject<DubboFutureCallBack> p) {
        return false;
    }

    @Override
    public void activateObject(PooledObject<DubboFutureCallBack> p) {

    }

    @Override
    public void passivateObject(PooledObject<DubboFutureCallBack> p) {

    }
}
