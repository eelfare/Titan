package com.yunji.titanrtx.cia.agent.log.listener;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.cia.agent.log.RulesStore;
import com.yunji.titanrtx.common.domain.cia.Rules;
import com.yunji.titanrtx.common.enums.RulesType;
import com.yunji.titanrtx.common.u.CollectionU;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public  class CiaConfigListener implements PathChildrenCacheListener, RulesStore {

    private final Map<RulesType,List<Rules>> rulesMap = new ConcurrentHashMap<>();

    @Override
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {

        if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED || event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED){
            log.info("节点发生变化,事件类型{},路径：{},数据：{}",event.getType(),event.getData().getPath(),new String(event.getData().getData()));

            String s =  new String(event.getData().getData());
            List<Rules> rules = JSON.parseArray(s, Rules.class);

            if (CollectionU.isEmpty(rules))return;

            Map<RulesType,List<Rules>> refreshRulesMap = new ConcurrentHashMap<>();
            for (Rules rule : rules){

                RulesType rulesType = rule.getRulesType();
                List<Rules> rulesList = refreshRulesMap.get(rulesType);
                if (CollectionU.isEmpty(rulesList)){
                    rulesList = new ArrayList<>();
                    refreshRulesMap.put(rulesType,rulesList);
                };
                rulesList.add(rule);
            }
            rulesMap.clear();
            rulesMap.putAll(refreshRulesMap);
        }
    }

    @Override
    public List<Rules> rules(RulesType type) {
        return rulesMap.getOrDefault(type,new ArrayList<>());
    }
}
