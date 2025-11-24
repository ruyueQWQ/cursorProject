package com.ai.algorithmqa.domain.dto;

public record AlgorithmUpdateRequest(
        Long id,
        String name,
        String coreIdea,
        String stepBreakdown,
        String timeComplexity,
        String spaceComplexity,
        String codeSnippet,
        String visualizationHint,
        String mermaidCode,
        String animationUrl) {
}
