package com.yunji.titanrtx.cia.agent.annotation;

import com.yunji.titanrtx.cia.agent.log.Stream;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.AccessLog;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StreamHandler implements ApplicationContextAware,Stream<AccessLog> {

    private List<Stream<AccessLog>> streamChain = new ArrayList<>();;

    @SuppressWarnings("unchecked")
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(StreamAnnotation.class);
        Map<Integer,Object>  sortMap= new HashMap<>();
        List<Integer> indexList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : beans.entrySet()){
            int i = entry.getValue().getClass().getAnnotation(StreamAnnotation.class).index();
            sortMap.put(i,entry.getValue());
            indexList.add(i);
        }
        Collections.sort(indexList);
        for (Integer i : indexList){
            streamChain.add((Stream<AccessLog>) sortMap.get(i));
        }
    }


    @Override
    public AccessLog onEvent(AccessLog accessLog) {
        AccessLog log = null;
        for (Stream<AccessLog> stream : streamChain){
             log = stream.onEvent(accessLog);
            if (null == log){
                return null;
            };
        }
        return log;
    }
}
