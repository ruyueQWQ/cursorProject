package com.ai.algorithmqa.domain.dto;

import java.util.List;

public record AlgorithmVisualizationResponse(
        Long topicId,
        String topicTitle,
        String overview,
        String category,
        Integer difficultyLevel,
        List<AlgorithmBlock> algorithms
) {

    public record AlgorithmBlock(
            Long id,
            String name,
            String coreIdea,
            String stepBreakdown,
            String timeComplexity,
            String spaceComplexity,
            String codeSnippet,
            String visualizationHint
    ) {
    }
}

