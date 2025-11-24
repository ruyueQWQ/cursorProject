package com.ai.algorithmqa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long topicId;

    private String content;

    private String keywords;

    /**
     * Persisted as JSON array string for lightweight semantic search.
     */
    private String embeddingJson;

    @TableField(exist = false)
    private double score;
}

