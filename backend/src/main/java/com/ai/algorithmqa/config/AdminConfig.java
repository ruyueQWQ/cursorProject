package com.ai.algorithmqa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "admin")
public class AdminConfig {
    private String jwtSecret;
    private Long jwtExpiration;
}
