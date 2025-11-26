package com.ai.algorithmqa.service.impl;

import com.ai.algorithmqa.domain.entity.SearchClickLog;
import com.ai.algorithmqa.mapper.QaLogMapper;
import com.ai.algorithmqa.mapper.SearchClickLogMapper;
import com.ai.algorithmqa.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final SearchClickLogMapper searchClickLogMapper;
    private final QaLogMapper qaLogMapper;

    @Override
    public void logClick(Long topicId, String topicTitle) {
        SearchClickLog logEntry = new SearchClickLog();
        logEntry.setTopicId(topicId);
        logEntry.setTopicTitle(topicTitle);
        logEntry.setCreatedAt(LocalDateTime.now());
        searchClickLogMapper.insert(logEntry);
        log.debug("记录点击: topicId={}, topicTitle={}", topicId, topicTitle);
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取 Top 10 搜索问题
        List<Map<String, Object>> topQuestions = qaLogMapper.selectTopQuestions(10);
        stats.put("topQuestions", topQuestions);

        // 获取 Top 10 点击算法
        List<Map<String, Object>> topClicks = searchClickLogMapper.selectTopClicks(10);
        stats.put("topClicks", topClicks);

        log.info("获取统计数据: topQuestions={}, topClicks={}",
                topQuestions.size(), topClicks.size());

        return stats;
    }
}
