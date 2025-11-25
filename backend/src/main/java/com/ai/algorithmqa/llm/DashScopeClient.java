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
                    "input", Map.of("prompt", prompt));

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

    public String streamChat(String prompt, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            try {
                String mock = """
                        【模拟回答】当前未配置阿里云DashScope API Key。
                        你可以在 application.yml 或环境变量 DASHSCOPE_API_KEY 中配置后获得真实回答。
                        下面是基于知识库模板给出的示例回答：
                        """ + prompt;
                emitter.send(mock);
                emitter.complete();
                return mock;
            } catch (Exception e) {
                emitter.completeWithError(e);
                return "";
            }
        }

        StringBuilder fullContent = new StringBuilder();
        try {
            Map<String, Object> body = Map.of(
                    "model", properties.model(),
                    "input", Map.of("prompt", prompt),
                    "parameters", Map.of("incremental_output", true));

            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .readTimeout(java.time.Duration.ofSeconds(300))
                    .build();

            String jsonBody = objectMapper.writeValueAsString(body);
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(properties.endpoint())
                    .post(okhttp3.RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + properties.apiKey())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-DashScope-SSE", "enable")
                    .build();

            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Unexpected code " + response);
                }

                java.io.BufferedReader reader = new java.io.BufferedReader(response.body().charStream());
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if (data.isEmpty())
                            continue;

                        JsonNode node = objectMapper.readTree(data);
                        String content = node.path("output").path("text").asText();
                        if (content != null && !content.isEmpty()) {
                            // 使用 JSON 包装 content 以保留换行符等格式
                            emitter.send(Map.of("content", content));
                            fullContent.append(content);
                        }

                        if (node.path("output").path("finish_reason").asText().equals("stop")) {
                            break;
                        }
                    }
                }
                emitter.complete();
            }
        } catch (Exception e) {
            log.error("DashScope 流式调用失败", e);
            emitter.completeWithError(e);
        }
        return fullContent.toString();
    }
}
