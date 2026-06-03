package com.english.platform.service;

import com.english.platform.dto.TestSubmitDTO;

import java.util.Map;

public interface TestService {
    /** 生成一次测试题目 */
    Map<String, Object> generateTest(Long userId, String questionType, Integer difficulty, Integer count);

    /** 提交测试答案并评分 */
    Map<String, Object> submitTest(Long userId, TestSubmitDTO dto);

    /** 获取单次测试详情 */
    Map<String, Object> getTestDetail(String sessionId);
}
