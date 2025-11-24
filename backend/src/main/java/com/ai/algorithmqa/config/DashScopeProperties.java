package com.ai.algorithmqa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "dashscope")
public record DashScopeProperties(
        String apiKey,
        String model,
        String endpoint,
        String embeddingModel,
        String embeddingEndpoint
) {
}

