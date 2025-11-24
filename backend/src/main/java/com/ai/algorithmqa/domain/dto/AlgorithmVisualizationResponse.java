package com.ai.algorithmqa.domain.dto;

import java.util.List;
// 定义一个不可变的记录类，用于表示算法可视化响应
public record AlgorithmVisualizationResponse(
        Long topicId,          // 主题ID
        String topicTitle,     // 主题标题
        String overview,       // 概述
        String category,       // 类别
        Integer difficultyLevel, // 难度级别
        List<AlgorithmBlock> algorithms // 算法块列表
) {

    // 定义一个不可变的记录类，用于表示算法块
    public record AlgorithmBlock(
            Long id,                // 算法块ID
            String name,           // 算法名称
            String coreIdea,       // 核心思想
            String stepBreakdown,  // 步骤分解
            String timeComplexity, // 时间复杂度
            String spaceComplexity, // 空间复杂度
            String codeSnippet,     // 代码片段
            String visualizationHint // 可视化提示
    ) {
    }
}
