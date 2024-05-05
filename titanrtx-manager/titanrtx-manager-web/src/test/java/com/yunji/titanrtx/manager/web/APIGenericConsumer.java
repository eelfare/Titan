package com.yunji.titanrtx.manager.web;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.service.GenericService;
import org.hibernate.validator.constraints.EAN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIGenericConsumer {


    public static void main(String[] args) {
       for (int i = 0 ; i < 5; i ++){

           ApplicationConfig application = new ApplicationConfig();
           application.setName("api-generic-consumer");

           RegistryConfig registry = new RegistryConfig();
           registry.setAddress("zookeeper://127.0.0.1:2181");

           //application.setRegistry(registry);

           ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
           // 弱类型接口名
           reference.setInterface("com.alibaba.dubbo.demo.DemoService");
           // 声明为泛化接口
           reference.setGeneric(true);
           reference.setUrl("dubbo://172.16.13.239:20881");
           reference.setApplication(application);

           // 用org.apache.dubbo.rpc.service.GenericService可以替代所有接口引用
           GenericService genericService = reference.get();

           String simpleType = (String) genericService.$invoke("sayHell1o", new String[]{String.class.getName()}, new Object[]{"world"});
           System.out.println(simpleType);


           List<String> list = new ArrayList<>();
           list.add("add");
           String multipleType = (String) genericService.$invoke("sayHello", new String[]{String.class.getName(),List.class.getName()}, new Object[]{"world",list});
           System.out.println(multipleType);

           Map<String, String> map = new HashMap<>();
          // map.put("class","com.alibaba.dubbo.demo.Student");
           map.put("id","1");
           map.put("name","GLGGAG");
           Object student = genericService.$invoke("sayHello", new String[]{String.class.getName(),"com.alibaba.dubbo.demo.Student"}, new Object[]{"world",map});
           System.out.println(student);
           reference.destroy();

       }


    }

}
