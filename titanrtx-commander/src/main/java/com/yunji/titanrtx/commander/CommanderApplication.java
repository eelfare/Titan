package com.yunji.titanrtx.commander;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource(locations = {"classpath:dubbo-content.xml"})
@SpringBootApplication(scanBasePackages = "com.yunji")
public class CommanderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommanderApplication.class, args);
    }

}

