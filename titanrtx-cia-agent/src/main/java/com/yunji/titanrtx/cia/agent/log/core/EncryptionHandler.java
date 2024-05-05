package com.yunji.titanrtx.cia.agent.log.core;

import com.yunji.titanrtx.cia.agent.annotation.StreamAnnotation;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.AccessLog;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.cia.Rules;
import com.yunji.titanrtx.common.enums.RulesType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@StreamAnnotation(index = 1)
public class EncryptionHandler extends AbstractStream {

    @Override
    protected RulesType rulesType() {
        return RulesType.ENCRYPTION;
    }

    @Override
    protected AccessLog doEvent(Rules rules, AccessLog accessLog) {

        String rulesParam = rules.getParam();
        String logParam = accessLog.getParam();

        log.debug("未加密之前：log:{},规则：{}..................................................",accessLog,rules);
        Map<String, String> paramsMap = new HashMap<>();
        String[] paramPair = logParam.split(GlobalConstants.PARAMS_SEGMENT);
        for (String pp : paramPair){
            String[] pair = pp.split(GlobalConstants.PARAMS_PAIR_SEGMENT);
            if (pair.length == 2){
                paramsMap.put(pair[0],pair[1]);
            }
        }

        String paramValue = paramsMap.get(rulesParam);
        if (StringUtils.isNoneBlank(paramValue)){
            paramsMap.put(rulesParam, DigestUtils.md5DigestAsHex(paramValue.getBytes()));
        }

        StringBuilder sb = new StringBuilder();
        paramsMap.forEach((key, value) -> sb.append(key).append("=").append(value).append("&"));
        sb.replace(sb.length()-1 ,sb.length(),"");
        accessLog.setParam( sb.toString());
        log.debug("加密之后：log:{}.................................................................",accessLog);
        return accessLog;
    }

}
