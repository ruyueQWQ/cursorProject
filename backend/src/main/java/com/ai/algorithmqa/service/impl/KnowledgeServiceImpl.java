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
        String[] parts = source.split("(?<=。|！|？|\\.)");
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
     * 通过 MyBatis-Plus 的查询条件组合关键词过滤与模糊匹配，
     * 再用简单的 token 匹配计算粗略得分，返回 Top-K 参考片段。
     */
    @Override
    public List<ReferenceChunk> search(String query, List<String> filters, int topK) {
        int limit = topK > 0 ? topK : DEFAULT_TOP_K;

        LambdaQueryWrapper<KnowledgeChunk> wrapper = Wrappers.lambdaQuery();

        if (filters != null && !filters.isEmpty()) {
            filters.forEach(filter ->
                    wrapper.and(w -> w.like(KnowledgeChunk::getKeywords, filter))
            );
        }

// 把主查询条件也改成匹配 keywords（或 keyword+content 二选一）
        wrapper.like(KnowledgeChunk::getKeywords, query);
        wrapper.last("limit " + (limit * 2));
        List<KnowledgeChunk> chunks = chunkMapper.selectList(wrapper);
        if (chunks.isEmpty()) {
            log.info("知识检索无结果 query={}, filters={}", query, filters);
            return List.of();
        }

        Map<Long, KnowledgeTopic> topicMap = topicMapper.selectBatchIds(
                chunks.stream().map(KnowledgeChunk::getTopicId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(KnowledgeTopic::getId, t -> t));

        List<ReferenceChunk> results = chunks.stream()
                .peek(chunk -> chunk.setScore(similarityScore(chunk.getContent(), query)))
                .sorted(Comparator.comparingDouble(KnowledgeChunk::getScore).reversed())
                .limit(limit)
                .map(chunk -> {
                    KnowledgeTopic topic = topicMap.get(chunk.getTopicId());
                    return new ReferenceChunk(
                            chunk.getTopicId(),
                            topic != null ? topic.getTitle() : "未知主题",
                            chunk.getContent(),
                            chunk.getScore()
                    );
                })
                .collect(Collectors.toList());
        log.debug("知识检索命中 {} 条（limit={}）", results.size(), limit);
        return results;
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
                Wrappers.lambdaQuery(AlgorithmDetail.class).eq(AlgorithmDetail::getTopicId, topicId)
        );
        List<AlgorithmVisualizationResponse.AlgorithmBlock> blocks = details.stream()
                .map(detail -> new AlgorithmVisualizationResponse.AlgorithmBlock(
                        detail.getId(),
                        detail.getName(),
                        detail.getCoreIdea(),
                        detail.getStepBreakdown(),
                        detail.getTimeComplexity(),
                        detail.getSpaceComplexity(),
                        detail.getCodeSnippet(),
                        detail.getVisualizationHint()
                ))
                .toList();
        log.info("返回可视化数据 topicId={}, algorithms={}", topicId, blocks.size());
        return new AlgorithmVisualizationResponse(
                topic.getId(),
                topic.getTitle(),
                topic.getOverview(),
                topic.getCategory(),
                topic.getDifficultyLevel(),
                blocks
        );
    }
}

