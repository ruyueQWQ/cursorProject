package com.ai.algorithmqa.service;

import java.util.List;

/**
 * 抽象 embedding 能力，便于后续替换为不同的向量服务。
 */
public interface EmbeddingService {

    /**
     * 将自然语言文本编码为向量表示。
     */
    List<Double> embed(String text);
}

