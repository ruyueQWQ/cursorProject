package com.ai.algorithmqa.service;

import com.ai.algorithmqa.domain.dto.AlgorithmVisualizationResponse;
import com.ai.algorithmqa.domain.dto.KnowledgeIngestRequest;
import com.ai.algorithmqa.domain.dto.ReferenceChunk;

import java.util.List;

/**
 * 课程知识库相关服务：负责知识导入、分片、检索。
 */
public interface KnowledgeService {

    /**
     * 导入课程知识点（主题 + 算法），并拆分成检索片段。
     */
    void ingest(KnowledgeIngestRequest request);

    /**
     * 根据问题/关键词检索知识片段，返回给 RAG 流程使用。
     */
    List<ReferenceChunk> search(String query, List<String> filters, int topK);

    /**
     * 获取指定主题下的算法可视化信息，供前端展示。
     */
    AlgorithmVisualizationResponse findVisualizationByTopic(Long topicId);
}

