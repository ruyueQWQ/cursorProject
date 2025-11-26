package com.ai.algorithmqa.mapper;

import com.ai.algorithmqa.domain.entity.QaLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QaLogMapper extends BaseMapper<QaLog> {

    /**
     * 查询搜索次数最多的问题
     * 
     * @param limit 返回数量
     * @return 统计结果列表
     */
    @org.apache.ibatis.annotations.Select("SELECT question, COUNT(*) as count " +
            "FROM qa_log " +
            "GROUP BY question " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    java.util.List<java.util.Map<String, Object>> selectTopQuestions(int limit);
}
