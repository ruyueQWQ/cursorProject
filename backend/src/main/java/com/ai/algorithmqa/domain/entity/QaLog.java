package com.ai.algorithmqa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_log")
public class QaLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String question;

    private String answer;

    private String referenceSummary;

    private Long latencyMs;

    private String model;

    private LocalDateTime createdAt;
}

