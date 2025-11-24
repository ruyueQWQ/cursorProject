package com.ai.algorithmqa.service;

import com.ai.algorithmqa.domain.dto.QaRequest;
import com.ai.algorithmqa.domain.dto.QaResponse;
import com.ai.algorithmqa.domain.dto.ReferenceChunk;
import com.ai.algorithmqa.llm.DashScopeClient;
import com.ai.algorithmqa.llm.LlmAnswer;
import com.ai.algorithmqa.mapper.QaLogMapper;
import com.ai.algorithmqa.service.impl.QaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QaServiceImplTest {

    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private DashScopeClient dashScopeClient;
    @Mock
    private QaLogMapper qaLogMapper;
    @InjectMocks
    private QaServiceImpl qaService;

    @Test
    void answerShouldReturnModelContent() {
        ReferenceChunk ref = new ReferenceChunk(1L, "分治策略", "分治将问题拆成子问题", 0.9);
        when(knowledgeService.search(any(), any(), anyInt())).thenReturn(List.of(ref));
        when(dashScopeClient.chat(any())).thenReturn(new LlmAnswer("解析内容", "mock-model", 120L));

        QaRequest request = new QaRequest("解释分治思想", List.of("分治"), 2, true);
        QaResponse response = qaService.answer(request);

        assertThat(response.answer()).isEqualTo("解析内容");
        assertThat(response.references()).hasSize(1);
        assertThat(response.model()).isEqualTo("mock-model");
        verify(qaLogMapper).insert(any());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(dashScopeClient).chat(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("解释分治思想");
    }
}

