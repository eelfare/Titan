package com.yunji.titanrtx.manager.web.config;

import com.yunji.titanrtx.common.enums.Platform;
import com.yunji.titanrtx.plugin.monitor.Monitor;
import com.yunji.titanrtx.plugin.monitor.ali.ECSMonitor;
import com.yunji.titanrtx.plugin.monitor.tencent.VCMMonitor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SystemConfig {

    public static final int MAX_CONCURRENT = 4 * 10000;
    public static final int MIN_CONCURRENT = 1000;

//    @Value("${httpConcurrent:10000}")
//    private volatile int httpConcurrent;

    @Value("${dubboConcurrent:10000}")
    private volatile int dubboConcurrent;

    @Value("${parent.template.path:/usr/local/yunji/titanx/docTemp}")
    private String parentTempPath;

    @Value("${son.template.path:/usr/local/yunji/titanx/docTemp}")
    private String sonTempPath;

    @Value("${monitor.regionId}")
    private String regionId;

    @Value("${monitor.accessId}")
    private String accessId;

    @Value("${monitor.secretKey}")
    private String secretKey;

    @Value("${cloudPlatform}")
    private String cloudPlatform;


    @Bean
    public Monitor platformMonitor() {
        if (StringUtils.equalsIgnoreCase(cloudPlatform, Platform.TENCENT.toString())) {
            return new VCMMonitor(regionId, accessId, secretKey);
        }
        return new ECSMonitor(regionId, accessId, secretKey);
    }

}
