package com.ai.algorithmqa;

import com.ai.algorithmqa.config.DashScopeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DashScopeProperties.class)
public class AlgorithmQaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgorithmQaApplication.class, args);
    }
}

