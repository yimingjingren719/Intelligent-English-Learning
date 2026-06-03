package com.english.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.entity.ErrorRecord;

import java.util.Map;

public interface ErrorService {
    /** 分页查询用户错题 */
    Page<ErrorRecord> pageQuery(Long userId, Long page, Long size);

    /** 获取错题详情（含题目信息） */
    Map<String, Object> getDetail(Long errorId);

    /** 标记错题已掌握 */
    void markMastered(Long errorId, Long userId);

    /** 重新练习错题 */
    Map<String, Object> practiceWrongQuestions(Long userId, Integer count);
}
