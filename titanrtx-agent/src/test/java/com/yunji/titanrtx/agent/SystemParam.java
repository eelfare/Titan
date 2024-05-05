package com.yunji.titanrtx.agent;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: bryan
 * @Date: 2019-10-28 14:23
 * @Version 1.0
 */
public class SystemParam {
    public static void main(String[] args) {
        Properties properties = System.getProperties();
        Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> next = iterator.next();
            System.out.println(next.getKey() + ":"+next.getValue());
        }
    }
}
