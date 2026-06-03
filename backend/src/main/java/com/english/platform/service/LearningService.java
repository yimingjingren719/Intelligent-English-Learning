package com.english.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.entity.LearningRecord;

import java.util.List;
import java.util.Map;

public interface LearningService {
    /** 分页查询用户学习记录 */
    Page<LearningRecord> pageQuery(Long userId, Long page, Long size, String recordType);

    /** 获取学习统计 */
    Map<String, Object> getStatistics(Long userId);
    Map<String, Object> getStatistics(Long userId, Integer recentCount);

    /** 获取详细统计（按题型、最近成绩等） */
    Map<String, Object> getDetailedStatistics(Long userId, Integer recentCount);

    /** 记录在线学习时长 */
    void recordOnlineTime(Long userId, Integer seconds);

    /** 获取日历数据 */
    List<Map<String, Object>> getCalendarData(Long userId, int year, int month);
}
