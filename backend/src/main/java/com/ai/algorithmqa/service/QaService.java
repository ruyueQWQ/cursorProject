package com.ai.algorithmqa.service;

import com.ai.algorithmqa.domain.dto.QaRequest;
import com.ai.algorithmqa.domain.dto.QaResponse;

/**
 * 问答业务接口，负责 orchestrate 检索、Prompt 构造、大模型调用与日志落库。
 */
public interface QaService {

    /**
     * 根据用户问题生成回答。
     *
     * @param request 包含原始提问、可选检索过滤、TopK 等信息
     * @return 模型回答 + 引用片段 + 元信息
     */
    QaResponse answer(QaRequest request);

    org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamAnswer(QaRequest request);
}
