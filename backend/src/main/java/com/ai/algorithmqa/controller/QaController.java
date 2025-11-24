package com.ai.algorithmqa.controller;

import com.ai.algorithmqa.common.ApiResponse;
import com.ai.algorithmqa.domain.dto.QaRequest;
import com.ai.algorithmqa.domain.dto.QaResponse;
import com.ai.algorithmqa.service.QaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问答模块入口，负责接收前端提问请求并转交给 {@link QaService}。
 * 统一返回 {@link ApiResponse}，方便前端处理状态与数据。
 */
@Slf4j
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    /**
     * 处理一次问答请求：
     * <ol>
     *     <li>校验问题内容、可选的检索过滤条件；</li>
     *     <li>委托服务层执行 RAG + DashScope 调用；</li>
     *     <li>封装为统一响应结构返回给前端。</li>
     * </ol>
     */
    @PostMapping
    public ApiResponse<QaResponse> ask(@Valid @RequestBody QaRequest request) {
        log.info("收到问答请求，question={}, filters={}, topK={}",
                request.question(), request.contextFilters(), request.topK());
        return ApiResponse.ok(qaService.answer(request));
    }
}

