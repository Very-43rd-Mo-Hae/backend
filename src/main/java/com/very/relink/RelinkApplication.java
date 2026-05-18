package com.very.relink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RelinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelinkApplication.class, args);
    }

}
