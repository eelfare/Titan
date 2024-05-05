package com.yunji.titanrtx.cia.agent.log.core;

import com.yunji.titanrtx.cia.agent.log.RulesStore;
import com.yunji.titanrtx.cia.agent.log.Stream;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.AccessLog;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.cia.Rules;
import com.yunji.titanrtx.common.enums.RulesType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
public abstract class AbstractStream implements Stream<AccessLog> {

    @Resource
    protected RulesStore rulesStore;

    @Override
    public AccessLog onEvent(AccessLog accessLog) {
        if (accessLog == null) return null;

        List<Rules> rules = rulesStore.rules(rulesType());
        for (Rules rule : rules) {
            String domain = rule.getDomain();
            String path = rule.getPath();
            String param = rule.getParam();

            if (StringUtils.equalsIgnoreCase(GlobalConstants.ALL, domain) || StringUtils.equalsIgnoreCase(accessLog.getDomain(), domain)) {
                if (StringUtils.equalsIgnoreCase(GlobalConstants.ALL, path) || StringUtils.equalsIgnoreCase(accessLog.getPath(), path)) {
                    if (StringUtils.equalsIgnoreCase(GlobalConstants.ALL, param) || StringUtils.containsIgnoreCase(accessLog.getParam(), param)) {
                        return doEvent(rule, accessLog);
                    }
                } else {
                    String template;
                    if (StringUtils.startsWithIgnoreCase(path, GlobalConstants.ALL)) {
                        template = path.substring(1);
                        if (StringUtils.endsWithIgnoreCase(accessLog.getPath(), template)) {
                            if (StringUtils.equalsIgnoreCase(GlobalConstants.ALL, param) || StringUtils.containsIgnoreCase(accessLog.getParam(), param)) {
                                return doEvent(rule, accessLog);
                            }
                        }
                    } else if (StringUtils.endsWithIgnoreCase(path, GlobalConstants.ALL)) {
                        template = path.substring(0, path.length() - 1);
                        if (StringUtils.startsWithIgnoreCase(accessLog.getPath(), template)) {
                            if (StringUtils.equalsIgnoreCase(GlobalConstants.ALL, param) || StringUtils.containsIgnoreCase(accessLog.getParam(), param)) {
                                return doEvent(rule, accessLog);
                            }
                        }
                    }
                }
            }
        }
        return accessLog;
    }

    protected abstract RulesType rulesType();


    protected abstract AccessLog doEvent(Rules rules, AccessLog log);

}
