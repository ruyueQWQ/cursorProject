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
        List<AlgorithmSection> algorithms
) {

    public record AlgorithmSection(
            @NotBlank String name,
            String coreIdea,
            String steps,
            String complexity,
            String codeSnippet,
            String visualizationHint
    ) {
    }
}

