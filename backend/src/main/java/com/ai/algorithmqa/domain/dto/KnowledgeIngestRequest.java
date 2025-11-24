package com.ai.algorithmqa.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record KnowledgeIngestRequest(
        @NotBlank String title,
        @NotBlank String category,
        String overview,
        List<String> keywords,
        Integer difficultyLevel,
        List<AlgorithmSection> algorithms) {
    // 表示知识内容的请求记录，包含标题、类别、概述、关键词、难度等级和算法部分
    public record AlgorithmSection(
            @NotBlank String name,
            String coreIdea,
            String steps,
            String complexity,
            String codeSnippet,
            String visualizationHint,
            String mermaidCode) {
        // 表示算法的部分，包含名称、核心思想、步骤、复杂度、代码片段和可视化提示
    }
}
