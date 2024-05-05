package com.yunji.titanrtx.agent;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class PersonObjectFactory implements PooledObjectFactory<Person> {
    @Override
    public PooledObject<Person> makeObject() throws Exception {
        Person dbConnection = new Person();
        return new DefaultPooledObject<>(dbConnection);
    }

    @Override
    public void destroyObject(PooledObject<Person> p) throws Exception {
    }

    @Override
    public boolean validateObject(PooledObject<Person> p) {
        return false;
    }

    @Override
    public void activateObject(PooledObject<Person> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<Person> p) throws Exception {

    }
}
