package com.ai.algorithmqa.llm;

import com.ai.algorithmqa.config.DashScopeProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashScopeClient {

    private final RestTemplate restTemplate;
    private final DashScopeProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmAnswer chat(String prompt) {
        Instant start = Instant.now();

        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            String mock = """
                    【模拟回答】当前未配置阿里云DashScope API Key。
                    你可以在 application.yml 或环境变量 DASHSCOPE_API_KEY 中配置后获得真实回答。
                    下面是基于知识库模板给出的示例回答：
                    """ + prompt;
            return new LlmAnswer(mock, "mock-model", Duration.between(start, Instant.now()).toMillis());
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", properties.model(),
                    "input", Map.of("prompt", prompt)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(properties.endpoint(), entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("output").path("text").asText();
            if (content == null || content.isBlank()) {
                content = root.toPrettyString();
            }
            long latency = Duration.between(start, Instant.now()).toMillis();
            return new LlmAnswer(content, properties.model(), latency);
        } catch (Exception e) {
            log.error("DashScope 调用失败", e);
            throw new RuntimeException("调用阿里云大模型失败：" + e.getMessage(), e);
        }
    }
}

