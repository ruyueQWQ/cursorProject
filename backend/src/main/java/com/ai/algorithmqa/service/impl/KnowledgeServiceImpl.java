package com.ai.algorithmqa.service.impl;

import com.ai.algorithmqa.domain.dto.AlgorithmVisualizationResponse;
import com.ai.algorithmqa.domain.dto.KnowledgeIngestRequest;
import com.ai.algorithmqa.domain.dto.ReferenceChunk;
import com.ai.algorithmqa.domain.entity.AlgorithmDetail;
import com.ai.algorithmqa.domain.entity.KnowledgeChunk;
import com.ai.algorithmqa.domain.entity.KnowledgeTopic;
import com.ai.algorithmqa.mapper.AlgorithmDetailMapper;
import com.ai.algorithmqa.mapper.KnowledgeChunkMapper;
import com.ai.algorithmqa.mapper.KnowledgeTopicMapper;
import com.ai.algorithmqa.service.EmbeddingService;
import com.ai.algorithmqa.service.KnowledgeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 负责知识库生命周期：导入/拆分/embedding/检索。
 * 这里使用 MySQL + 轻量 embedding 字段实现“关键词 + 粗语义”的混合检索。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final int DEFAULT_TOP_K = 4;

    private final KnowledgeTopicMapper topicMapper;
    private final AlgorithmDetailMapper detailMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将传入的课程数据写入 theme/detail/chunk 三张表；
     * 过程中会自动拆分段落并生成 embedding JSON。
     */
    @Override
    @Transactional
    public void ingest(KnowledgeIngestRequest request) {
        log.info("开始导入知识点 [{}]", request.title());
        KnowledgeTopic topic = new KnowledgeTopic();
        topic.setTitle(request.title());
        topic.setCategory(request.category());
        topic.setOverview(request.overview());
        topic.setKeywords(String.join(",", request.keywords() == null ? List.of() : request.keywords()));
        topic.setDifficultyLevel(request.difficultyLevel());
        topic.setTags(topic.getKeywords());
        topic.setCreatedAt(LocalDateTime.now());
        topic.setUpdatedAt(LocalDateTime.now());
        topicMapper.insert(topic);

        int chunkCount = 0;
        if (request.algorithms() != null) {
            for (KnowledgeIngestRequest.AlgorithmSection section : request.algorithms()) {
                AlgorithmDetail detail = new AlgorithmDetail();
                detail.setTopicId(topic.getId());
                detail.setName(section.name());
                detail.setCoreIdea(section.coreIdea());
                detail.setStepBreakdown(section.steps());
                detail.setTimeComplexity(section.complexity());
                detail.setSpaceComplexity(section.complexity());
                detail.setCodeSnippet(section.codeSnippet());
                detail.setVisualizationHint(section.visualizationHint());
                detail.setMermaidCode(section.mermaidCode());
                detailMapper.insert(detail);

                chunkCount += splitIntoChunks(topic.getId(), section, topic.getKeywords());
            }
        }
        log.info("导入知识点完成 topicId={}, chunkCount={}", topic.getId(), chunkCount);
    }

    /**
     * 将单个算法描述切成若干语义片段，便于检索与 prompt 注入。
     *
     * @return 实际写入的片段数量
     */
    private int splitIntoChunks(Long topicId, KnowledgeIngestRequest.AlgorithmSection section, String keywords) {
        String source = section.coreIdea() + "\n" + section.steps() + "\n" + section.complexity();
        // 优化切分逻辑：
        // 1. (?<!\d)\. -> 匹配前面不是数字的点号（避免切分 1. 2. 等序号）
        // 2. [。！？] -> 匹配中文句号、感叹号、问号
        String[] parts = source.split("(?<=。|！|？|(?<!\\d)\\.)");
        int count = 0;
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setTopicId(topicId);
            chunk.setContent(part.trim());
            chunk.setKeywords(keywords);
            chunk.setEmbeddingJson(toJson(embeddingService.embed(part)));
            chunkMapper.insert(chunk);
            count++;
        }
        return count;
    }

    private String toJson(List<Double> vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Embedding 序列化失败", e);
        }
    }

    /**
     * 纯语义检索：不使用 SQL LIKE 过滤，直接对所有候选进行向量相似度计算
     */
    @Override
    public List<ReferenceChunk> search(String query, List<String> filters, int topK) {
        int limit = topK > 0 ? topK : DEFAULT_TOP_K;

        LambdaQueryWrapper<KnowledgeChunk> wrapper = Wrappers.lambdaQuery();

        // 只应用用户指定的过滤条件（如算法类别），不对 query 本身做 LIKE 过滤
        if (filters != null && !filters.isEmpty()) {
            filters.forEach(filter -> wrapper.and(w -> w.like(KnowledgeChunk::getKeywords, filter)));
        }

        // 获取所有符合过滤条件的候选（如果没有过滤条件，则获取全部）
        // 为了性能考虑，设置一个合理的上限
        wrapper.last("limit 100");
        List<KnowledgeChunk> candidates = chunkMapper.selectList(wrapper);

        if (candidates.isEmpty()) {
            log.info("知识检索无结果 query={}, filters={}", query, filters);
            return List.of();
        }

        // 使用 embedding 计算语义相似度进行排序
        List<Double> queryEmbedding = embeddingService.embed(query);
        candidates.forEach(chunk -> {
            try {
                List<Double> chunkEmbedding = parseEmbedding(chunk.getEmbeddingJson());
                double similarity = cosineSimilarity(queryEmbedding, chunkEmbedding);
                chunk.setScore(similarity);
            } catch (Exception e) {
                log.warn("解析 embedding 失败，使用词频得分 chunkId={}", chunk.getId());
                chunk.setScore(similarityScore(chunk.getContent(), query));
            }
        });

        Map<Long, KnowledgeTopic> topicMap = topicMapper.selectBatchIds(
                candidates.stream().map(KnowledgeChunk::getTopicId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(KnowledgeTopic::getId, t -> t));

        List<ReferenceChunk> results = candidates.stream()
                .sorted(Comparator.comparingDouble(KnowledgeChunk::getScore).reversed())
                .limit(limit)
                .map(chunk -> {
                    KnowledgeTopic topic = topicMap.get(chunk.getTopicId());
                    return new ReferenceChunk(
                            chunk.getTopicId(),
                            topic != null ? topic.getTitle() : "未知主题",
                            chunk.getContent(),
                            chunk.getScore());
                })
                .collect(Collectors.toList());
        log.debug("知识检索命中 {} 条（limit={}），使用纯语义相似度排序", results.size(), limit);
        return results;
    }

    /**
     * 解析 JSON 格式的 embedding 向量
     */
    private List<Double> parseEmbedding(String embeddingJson) {
        try {
            return objectMapper.readValue(embeddingJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class));
        } catch (Exception e) {
            throw new RuntimeException("Embedding 反序列化失败", e);
        }
    }

    /**
     * 计算两个向量的余弦相似度
     */
    private double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("向量维度不匹配");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 基于词频的轻量得分函数，用于在没有真实向量数据库时的 fallback。
     */
    private double similarityScore(String content, String query) {
        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();
        int matches = 0;
        for (String token : lowerQuery.split(" ")) {
            if (lowerContent.contains(token)) {
                matches++;
            }
        }
        return matches / (double) Math.max(1, lowerQuery.split(" ").length);
    }

    @Override
    public AlgorithmVisualizationResponse findVisualizationByTopic(Long topicId) {
        KnowledgeTopic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            log.warn("尝试获取不存在的主题可视化 topicId={}", topicId);
            throw new IllegalArgumentException("未找到指定的知识主题");
        }
        List<AlgorithmDetail> details = detailMapper.selectList(
                Wrappers.lambdaQuery(AlgorithmDetail.class).eq(AlgorithmDetail::getTopicId, topicId));
        List<AlgorithmVisualizationResponse.AlgorithmBlock> blocks = details.stream()
                .map(detail -> new AlgorithmVisualizationResponse.AlgorithmBlock(
                        detail.getId(),
                        detail.getName(),
                        detail.getCoreIdea(),
                        detail.getStepBreakdown(),
                        detail.getTimeComplexity(),
                        detail.getSpaceComplexity(),
                        detail.getCodeSnippet(),
                        detail.getVisualizationHint(),
                        detail.getMermaidCode(),
                        detail.getAnimationUrl()))
                .toList();
        log.info("返回可视化数据 topicId={}, algorithms={}", topicId, blocks.size());
        return new AlgorithmVisualizationResponse(
                topic.getId(),
                topic.getTitle(),
                topic.getOverview(),
                topic.getCategory(),
                topic.getDifficultyLevel(),
                blocks);
    }

    @Override
    public List<String> getAvailableFilters() {
        List<KnowledgeTopic> topics = topicMapper.selectList(null);
        return topics.stream()
                .flatMap(topic -> {
                    if (topic.getKeywords() == null || topic.getKeywords().isBlank()) {
                        return java.util.stream.Stream.empty();
                    }
                    return java.util.Arrays.stream(topic.getKeywords().split(","));
                })
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
