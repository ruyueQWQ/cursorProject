package com.ai.algorithmqa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "dashscope")
// 定义DashScope的配置属性，使用record关键字创建不可变的数据类
public record DashScopeProperties(
        String apiKey,
        String model,
        String endpoint,
        String embeddingModel,
        String embeddingEndpoint
) {
}


