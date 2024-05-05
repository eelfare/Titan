package com.yunji.titanrtx.auto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 27/2/2020 5:19 下午
 * @Version 1.0
 */
@ImportResource(locations = {"classpath:dubbo-content.xml"})
@SpringBootApplication(
        scanBasePackages = {"com.yunji.titanrtx.auto", "com.yunji.titanrtx.common.alarm"},
        exclude = DataSourceAutoConfiguration.class
)
@EnableScheduling
public class AutoApplication {


    public static void main(String[] args) {
        SpringApplication.run(AutoApplication.class, args);
    }
}
