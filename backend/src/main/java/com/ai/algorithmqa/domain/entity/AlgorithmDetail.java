package com.ai.algorithmqa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("algorithm_detail")
public class AlgorithmDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long topicId;

    private String name;

    private String coreIdea;

    private String stepBreakdown;

    private String timeComplexity;

    private String spaceComplexity;

    private String codeSnippet;

    private String visualizationHint;
}

