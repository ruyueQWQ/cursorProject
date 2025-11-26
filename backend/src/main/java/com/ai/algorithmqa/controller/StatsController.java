package com.ai.algorithmqa.controller;

import com.ai.algorithmqa.common.ApiResponse;
import com.ai.algorithmqa.service.StatsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 记录算法点击
     */
    @PostMapping("/click")
    public ApiResponse<Void> logClick(@RequestBody LogClickRequest request) {
        log.debug("收到点击记录请求: topicId={}, topicTitle={}",
                request.getTopicId(), request.getTopicTitle());
        statsService.logClick(request.getTopicId(), request.getTopicTitle());
        return ApiResponse.ok(null);
    }

    /**
     * 获取统计仪表板数据
     */
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        log.info("获取统计仪表板数据");
        return ApiResponse.ok(statsService.getDashboardStats());
    }

    /**
     * 点击记录请求DTO
     */
    @Data
    public static class LogClickRequest {
        private Long topicId;
        private String topicTitle;
    }
}
