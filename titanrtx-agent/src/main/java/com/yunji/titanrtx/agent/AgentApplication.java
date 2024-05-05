package com.yunji.titanrtx.agent;

import com.yunji.titanrtx.agent.task.exec.AHCExecuteTask;
import com.yunji.titanrtx.agent.task.exec.http.ParamFetcher;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

@ImportResource(locations = {"classpath:dubbo-content.xml"})
@MapperScan(basePackages = "com.yunji.titanrtx.agent.mapper")
@SpringBootApplication
public class AgentApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AgentApplication.class, args);
//        ParamFetcher fetcher = context.getBean(ParamFetcher.class);
//        fetcher.paramFetch(1000);

    }
}

