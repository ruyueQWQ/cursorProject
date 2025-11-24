package com.ai.algorithmqa.domain.dto;

import java.util.List;

public record QaResponse(
        String answer,
        List<ReferenceChunk> references,
        String model,
        long latencyMs
) {
}

