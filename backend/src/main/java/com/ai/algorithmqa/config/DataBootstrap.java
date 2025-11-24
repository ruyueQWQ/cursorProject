package com.ai.algorithmqa.config;

import com.ai.algorithmqa.domain.dto.KnowledgeIngestRequest;
import com.ai.algorithmqa.mapper.KnowledgeTopicMapper;
import com.ai.algorithmqa.service.KnowledgeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataBootstrap {

    private final KnowledgeTopicMapper topicMapper;
    private final KnowledgeService knowledgeService;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/core_topics.yaml");
        if (!resource.exists()) {
            log.warn("未找到示例知识库数据 data/core_topics.yaml");
            return;
        }
        try (InputStream stream = resource.getInputStream()) {
            List<KnowledgeIngestRequest> topics = yamlMapper.readValue(stream, new TypeReference<>() {
            });
            int importedCount = 0;
            for (KnowledgeIngestRequest request : topics) {
                // 检查是否已存在同名主题
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.ai.algorithmqa.domain.entity.KnowledgeTopic> query = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.ai.algorithmqa.domain.entity.KnowledgeTopic>()
                        .eq(com.ai.algorithmqa.domain.entity.KnowledgeTopic::getTitle, request.title());

                Long count = topicMapper.selectCount(query);
                if (count != null && count > 0) {
                    log.info("发现已存在主题 [{}]，正在删除旧数据以重新导入...", request.title());
                    topicMapper.delete(query);
                }

                knowledgeService.ingest(request);
                importedCount++;
            }
            log.info("已自动导入 {} 条示例知识点", importedCount);
        }
    }
}
