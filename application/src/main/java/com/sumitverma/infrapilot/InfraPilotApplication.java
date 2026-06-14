package com.sumitverma.infrapilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InfraPilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfraPilotApplication.class, args);
    }
}