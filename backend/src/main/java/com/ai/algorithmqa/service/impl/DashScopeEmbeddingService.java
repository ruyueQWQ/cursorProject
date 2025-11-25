package com.ai.algorithmqa.service.impl;

import com.ai.algorithmqa.config.DashScopeProperties;
import com.ai.algorithmqa.service.EmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
            // 构建请求体： DashScope embedding API 期望的格式
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", properties.embeddingModel());

            // input 字段需要是一个包含 "texts" 数组的对象
            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("texts", List.of(text));
            requestBody.put("input", inputMap);

            // 序列化为 JSON 字符串
            String jsonBody = mapper.writeValueAsString(requestBody);

            // 设置必需的 HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + properties.apiKey());
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.embeddingEndpoint(),
                    entity,
                    String.class);

            JsonNode node = mapper.readTree(response.getBody());
            JsonNode embeddings = node.path("output").path("embeddings");

            // embeddings 是一个数组，我们需要第一个元素的 embedding 字段
            if (embeddings.isArray() && embeddings.size() > 0) {
                JsonNode vector = embeddings.get(0).path("embedding");
                if (!vector.isMissingNode() && vector.isArray()) {
                    List<Double> result = new ArrayList<>();
                    vector.forEach(value -> result.add(value.asDouble()));
                    log.info("调用 DashScope embedding 成功{}", result);
                    return result;

                }
            }

            log.warn("DashScope embedding 响应格式不正确，使用回退策略");
            return pseudoEmbedding(text);
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
