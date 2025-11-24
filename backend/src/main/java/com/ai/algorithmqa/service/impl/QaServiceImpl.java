package com.ai.algorithmqa.service.impl;

import com.ai.algorithmqa.domain.dto.QaRequest;
import com.ai.algorithmqa.domain.dto.QaResponse;
import com.ai.algorithmqa.domain.dto.ReferenceChunk;
import com.ai.algorithmqa.domain.entity.QaLog;
import com.ai.algorithmqa.llm.DashScopeClient;
import com.ai.algorithmqa.llm.LlmAnswer;
import com.ai.algorithmqa.mapper.QaLogMapper;
import com.ai.algorithmqa.service.KnowledgeService;
import com.ai.algorithmqa.service.QaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 问答主流程实现：
 * <ul>
 *     <li>按需执行知识检索得到引用片段；</li>
 *     <li>基于片段和提问构造 Prompt，调用 DashScope（通义千问）；</li>
 *     <li>记录问答日志，返回给前端。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QaServiceImpl implements QaService {

    private final KnowledgeService knowledgeService;
    private final DashScopeClient dashScopeClient;
    private final QaLogMapper qaLogMapper;

    /**
     * 将用户问题转化为最终回答，并记录日志。
     */
    @Override
    public QaResponse answer(QaRequest request) {
        log.debug("QA-Service 接到请求 question={}, useKB={}, topK={}, filters={}",
                request.question(), request.useKnowledgeBase(), request.topK(), request.contextFilters());
        List<ReferenceChunk> references = request.useKnowledgeBase()
                ? knowledgeService.search(request.question(), request.contextFilters(), request.topK() != null ? request.topK() : 4)
                : List.of();
        log.debug("知识检索返回 {} 条片段", references.size());

        String prompt = buildPrompt(request.question(), references);
        log.trace("构造 Prompt 完成，长度={} 字符", prompt.length());
        LlmAnswer answer = dashScopeClient.chat(prompt);
        log.info("DashScope 调用成功，模型={}，耗时={}ms", answer.model(), answer.latencyMs());

        QaLog logEntry = new QaLog();
        logEntry.setQuestion(request.question());
        logEntry.setAnswer(answer.content());
        logEntry.setReferenceSummary(references.stream()
                .map(ref -> ref.topicTitle() + ":" + ref.snippet())
                .collect(Collectors.joining(" | ")));
        logEntry.setLatencyMs(answer.latencyMs());
        logEntry.setModel(answer.model());
        logEntry.setCreatedAt(LocalDateTime.now());
        qaLogMapper.insert(logEntry);
        log.debug("问答日志写入完成 logId={}", logEntry.getId());

        return new QaResponse(answer.content(), references, answer.model(), answer.latencyMs());
    }

    /**
     * 根据问题与检索片段构造课程定制 Prompt，确保输出结构化、可追溯。
     */
    private String buildPrompt(String question, List<ReferenceChunk> references) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是一名《算法设计与分析》课程智能助教，需要给出条理清晰、严谨的回答。\n");
        if (!references.isEmpty()) {
            builder.append("以下是与问题相关的知识片段，请优先使用它们：\n");
            for (int i = 0; i < references.size(); i++) {
                ReferenceChunk ref = references.get(i);
                builder.append("【片段").append(i + 1).append(" - ").append(ref.topicTitle()).append("】")
                        .append(ref.snippet()).append("\n");
            }
        }
        builder.append("问题：").append(question).append("\n");
        builder.append("回答要求：\n")
                .append("1. 先概述算法思想，再给出关键步骤。\n")
                .append("2. 如果可以，附上时间复杂度和空间复杂度分析。\n")
                .append("3. 提供与其他算法的比较或适用场景。\n")
                .append("4. 以中文输出，如需代码示例请选择 Java 或 Python。\n");
        return builder.toString();
    }
}

