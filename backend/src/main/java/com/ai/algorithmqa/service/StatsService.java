package com.ai.algorithmqa.service;

import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatsService {

    /**
     * 记录算法点击
     * 
     * @param topicId    主题ID
     * @param topicTitle 主题标题
     */
    void logClick(Long topicId, String topicTitle);

    /**
     * 获取统计仪表板数据
     * 
     * @return 包含 topQuestions 和 topClicks 的统计数据
     */
    Map<String, Object> getDashboardStats();
}
