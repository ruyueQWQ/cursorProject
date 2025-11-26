package com.ai.algorithmqa.mapper;

import com.ai.algorithmqa.domain.entity.SearchClickLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 搜索点击日志 Mapper
 */
@Mapper
public interface SearchClickLogMapper extends BaseMapper<SearchClickLog> {

    /**
     * 查询点击次数最多的算法主题
     * 
     * @param limit 返回数量
     * @return 统计结果列表
     */
    @Select("SELECT topic_title as title, COUNT(*) as count " +
            "FROM search_click_log " +
            "GROUP BY topic_title " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopClicks(int limit);
}
