package com.ai.algorithmqa.service.impl;

import com.ai.algorithmqa.config.DashScopeProperties;
import com.ai.algorithmqa.service.EmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用阿里云 DashScope Embedding 服务生成向量，若未配置 Key 或调用失败则退回本地伪向量。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashScopeEmbeddingService implements EmbeddingService {

    private static final int FALLBACK_DIM = 24;

    private final RestTemplate restTemplate;
    private final DashScopeProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 优先调用官方 embedding API，否则走 fallback，保证开发阶段也可运行。
     */
    @Override
    public List<Double> embed(String text) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            return pseudoEmbedding(text);
        }

        try {
            Map<String, Object> input = new HashMap<>();
            input.put("model", properties.embeddingModel());
            input.put("input", text);

            ResponseEntity<String> response = restTemplate.postForEntity(properties.embeddingEndpoint(), input, String.class);
            JsonNode node = mapper.readTree(response.getBody());
            JsonNode vector = node.path("output").path("embedding");
            if (vector.isMissingNode()) {
                return pseudoEmbedding(text);
            }
            List<Double> result = new ArrayList<>();
            vector.forEach(value -> result.add(value.asDouble()));
            return result;
        } catch (Exception e) {
            log.warn("调用 DashScope embedding 失败，使用回退策略: {}", e.getMessage());
            return pseudoEmbedding(text);
        }
    }

    /**
     * 简单的 deterministic 伪向量实现，用于无网络/无 Key 的场景。
     */
    private List<Double> pseudoEmbedding(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        List<Double> vector = new ArrayList<>(FALLBACK_DIM);
        for (int i = 0; i < FALLBACK_DIM; i++) {
            int b = bytes[i % bytes.length];
            vector.add((double) ((b * (i + 3)) % 100) / 100.0);
        }
        return vector;
    }
}

