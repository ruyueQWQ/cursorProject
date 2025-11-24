package com.ai.algorithmqa.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QaRequest(
        @NotBlank(message = "问题不能为空")
        @Size(max = 500, message = "问题长度过长")
        String question,

        List<String> contextFilters,

        Integer topK,

        boolean useKnowledgeBase
) {
}

