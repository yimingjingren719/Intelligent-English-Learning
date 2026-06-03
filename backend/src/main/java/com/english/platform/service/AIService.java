package com.english.platform.service;

import java.util.Map;

public interface AIService {
    /** AI 智能答疑 */
    String chat(Long userId, String question, String contextType, Long questionId);

    /** 生成用户能力画像 */
    Map<String, Object> generateProfile(Long userId);

    /** 分析用户薄弱点 */
    Map<String, Object> analyzeWeakPoints(Long userId);

    /** 生成个性化学习建议 */
    Map<String, Object> suggestStudyPlan(Long userId);
}
