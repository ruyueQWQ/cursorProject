package com.ai.algorithmqa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 算法点击统计日志实体
 */
@Data
@TableName("search_click_log")
public class SearchClickLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 主题ID
     */
    private Long topicId;

    /**
     * 主题标题（冗余存储，便于统计）
     */
    private String topicTitle;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
