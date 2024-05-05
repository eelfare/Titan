package com.yunji.titanrtx.agent;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class PersonMain {


    public static void main(String[] args) throws Exception {
        PersonObjectFactory factory = new PersonObjectFactory();
        //设置对象池的相关参数
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(1);
        poolConfig.setMaxTotal(1);
        poolConfig.setMinIdle(1);
        //新建一个对象池,传入对象工厂和配置
        GenericObjectPool<Person> objectPool = new GenericObjectPool<>(factory, poolConfig);


        for (int i = 0 ; i < 100; i ++){
            if (i == 8){
                new Thread(objectPool::close).start();
            }
            Person   person = objectPool.borrowObject();
            System.out.println(person.getName());
            person.setName("hello:"+i);
            System.out.println(person.getName());
            objectPool.returnObject(person);
        }


    }




}
