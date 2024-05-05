package com.yunji.titanrtx.cia.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;


@ImportResource(locations= {"classpath:spring-content.xml"})
@SpringBootApplication
public class CiaAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CiaAgentApplication.class, args);
    }

}

