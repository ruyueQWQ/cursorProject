package com.ai.algorithmqa.domain.dto;

public record ReferenceChunk(
        Long topicId,
        String topicTitle,
        String snippet,
        double score
) {
}

