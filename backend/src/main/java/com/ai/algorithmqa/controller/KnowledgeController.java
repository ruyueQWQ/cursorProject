package com.ai.algorithmqa.controller;

import com.ai.algorithmqa.common.ApiResponse;
import com.ai.algorithmqa.domain.dto.AlgorithmVisualizationResponse;
import com.ai.algorithmqa.domain.dto.KnowledgeIngestRequest;
import com.ai.algorithmqa.domain.dto.ReferenceChunk;
import com.ai.algorithmqa.service.KnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库相关接口：负责录入课程知识、调试检索效果。
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 导入一条课程知识结构：主题 + 多个算法片段。
     * 在服务层内部会拆分、生成 embedding 并落库。
     */
    @PostMapping("/ingest")
    public ApiResponse<Void> ingest(@Valid @RequestBody KnowledgeIngestRequest request) {
        log.info("收到知识导入请求，title={}, algorithms={}",
                request.title(), request.algorithms() != null ? request.algorithms().size() : 0);
        knowledgeService.ingest(request);
        return ApiResponse.ok(null);
    }

    /**
     * 调试/可视化检索效果，方便确认知识库构建正确。
     *
     * @param query   学生问题或关键词
     * @param filters 额外的主题/标签过滤
     * @param topK    需要的片段数量
     */
    @GetMapping("/search")
    public ApiResponse<List<ReferenceChunk>> search(@RequestParam String query,
                                                    @RequestParam(required = false) List<String> filters,
                                                    @RequestParam(defaultValue = "4") int topK) {
        log.debug("知识检索 query={}, filters={}, topK={}", query, filters, topK);
        return ApiResponse.ok(knowledgeService.search(query, filters, topK));
    }

    @GetMapping("/topics/{topicId}/visualizations")
    public ApiResponse<AlgorithmVisualizationResponse> visualization(@PathVariable Long topicId) {
        log.info("获取可视化数据 topicId={}", topicId);
        return ApiResponse.ok(knowledgeService.findVisualizationByTopic(topicId));
    }
}

