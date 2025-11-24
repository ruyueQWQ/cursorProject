package com.ai.algorithmqa.domain.dto;

public record AlgorithmCreateRequest(
        Long topicId,
        String name,
        String coreIdea,
        String stepBreakdown,
        String timeComplexity,
        String spaceComplexity,
        String codeSnippet,
        String visualizationHint,
        String mermaidCode) {
}
