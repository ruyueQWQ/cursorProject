package com.ai.algorithmqa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_topic")
public class KnowledgeTopic {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String category;

    private String overview;

    private String keywords;

    private Integer difficultyLevel;

    private String tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

