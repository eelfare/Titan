package com.yunji.titanrtx.agent.boot;

import com.yunji.titanrtx.common.domain.statistics.QpsCollectInfo;
import com.yunji.titanrtx.common.register.Register;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-10-29 15:37
 * @Version 1.0
 */
@Slf4j
@Data
@Component
public class QpsCollectRegister implements Register {
    private Map<String, List<Integer>> mapRequest;
private Map<String, List<QpsCollectInfo>> mapResponse;

    @Override
    @PostConstruct
    public void register() {
        mapRequest = new HashMap<>();
        mapResponse = new HashMap<>();
    }
}
